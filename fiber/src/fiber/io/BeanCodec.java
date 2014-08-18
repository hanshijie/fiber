package fiber.io;

import java.util.Map;

import fiber.bean.AllBeans;

public final class BeanCodec extends ProtocolCodec {
	private final Map<Integer, BeanHandler<?>> handlerStub;
	public BeanCodec(final Map<Integer, BeanHandler<?>> handlerStub) {	
		this.handlerStub = handlerStub;
	}

	@Override
	public void decode(final IOSession session, OctetsStream is) {
		final Map<Integer, Bean<?>> beanStub = AllBeans.get();
		int step = 0;
		int lastHead = 0;
		//Log.fatal(is.toOctets().dump().toString());
		try {
			while(!is.empty()) {
				step = 0;
				lastHead = is.getHead();
				int type =  is.unmarshalUInt();
				int size = is.unmarshalUInt();
				
				step = 1;
				BeanHandler<?> handler = handlerStub.get(type);
				if(handler == null) {
					Log.err("[session-%d] unhandle type:%d", type);
					session.close();
					return;
				}
				Bean<?> bean = beanStub.get(type);
				if(bean == null) {
					Log.err("[session-%d] unkown bean type:%d", session.getId(), type);
					session.close();
					return;
				}
				if(size > bean.maxsize()) {
					Log.err("[session-%d] bean<%s> type:%d size:%d exceed maxsize:%d",
						session.getId(), bean.getClass().getSimpleName(), type, size, bean.maxsize());
					session.close();
					return;
				}
				if(is.size() < size) {
					is.setHead(lastHead);
					return;
				}
				OctetsStream data = is.getReadOnlySub(is.getHead(), size);
				final Bean<?> newBean = bean.create();
				newBean.unmarshal(data);
				is.skip(size);
				/*
				try {
					handler.process(session, newBean);
				} catch (Exception e) {
					Log.err("[session-%d] bean:%s process exception:%s",
							session.getId(), bean, e);
					session.close();
					return;
				}
				*/
				session.getHandler().getManager().onReceiveMessage(session, newBean);
			}
		} catch (MarshalException e) {
			if(step == 0) {
				is.setHead(lastHead);
				//Log.fatal("remain:%s", is.toOctets().dump().toString());
			} else {
				Log.warn("[session-%d] unmarshal fail!", session.getId());
				//Log.trace(is.toOctets().dump().toString());
				session.close();
			}
		}

	}

	@Override
	public Octets encode(IOSession session, Object obj) {
		Bean<?> bean = (Bean<?>)obj;
		//if(data.size() > bean.getMaxSize()) {
		//	Log.alert("bean<%s> size:%d exceed maxsize:%d", bean.getClass().getSimpleName(), data.size(), bean.getMaxSize());
		//}
		return encode(bean);
		//session.write(encode(bean));
	}
	
	public static Octets encode(Bean<?> bean) {
		OctetsStream os = tempOss.get();
		os.clear();
		final int HEADER_MAX_SIZE = 10;
		os.setTail(HEADER_MAX_SIZE);
		os.marshal(bean);
		int end = os.getTail();
		int bodysize = end - HEADER_MAX_SIZE;
		int type = bean.type();
		int start = HEADER_MAX_SIZE - OctetsStream.marshalUIntLen(bodysize) - OctetsStream.marshalUIntLen(type);
		os.setTail(start);
		os.marshalUInt(type);
		os.marshalUInt(bodysize);
		return Octets.create(os.getData(), start, end - start);	
	}
	
	private final static ThreadLocal<OctetsStream> tempOss = new ThreadLocal<OctetsStream>() {
		@Override
		protected OctetsStream initialValue() { return OctetsStream.create(1024); }
	};
	//private final OctetsStream tempHeadOs = OctetsStream.create(10);
	//private final OctetsStream tempBodyOs = OctetsStream.create(1024);
}
