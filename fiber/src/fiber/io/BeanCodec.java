package fiber.io;

import java.util.Map;

import fiber.bean.AllBeans;
import static fiber.io.Log.log;

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
					log.error("[session-{}] unhandle type:{}", session.getId(), type);
					session.close();
					return;
				}
				Bean<?> bean = beanStub.get(type);
				if(bean == null) {
					log.error("[session-{}] unkown bean type:{}", session.getId(), type);
					session.close();
					return;
				}
				if(size > bean.maxsize()) {
					log.error("[session-{}] bean<{}> type:{} size:{} exceed maxsize:{}",
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
					log.error("[session-{}] bean:{} process exception:{}",
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
				//Log.fatal("remain:{}", is.toOctets().dump().toString());
			} else {
				log.warn("[session-{}] unmarshal fail!", session.getId());
				//Log.trace(is.toOctets().dump().toString());
				session.close();
			}
		}

	}

	@Override
	public Octets encode(IOSession session, Object obj) {
		Bean<?> bean = (Bean<?>)obj;
		//if(data.size() > bean.getMaxSize()) {
		//	Log.alert("bean<{}> size:{} exceed maxsize:{}", bean.getClass().getSimpleName(), data.size(), bean.getMaxSize());
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
