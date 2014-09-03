package fiber.app.server;

import fiber.db.Enviroment;
import fiber.db.TValue;
import fiber.db.Table;
import fiber.db.TableMem;
import fiber.db.TablePer;
import fiber.db.Transaction;
import fiber.db.WKey;
import fiber.db.WValue;
import fiber.io.Log;
import fiber.bean.*;
import static fiber.bean._.*;

public class DB extends Enviroment {
	
	/////////////////////////////////////////////////////////
	//  db table defines 
	/////////////////////////////////////////////////////////
	public final static Table tUser = new TableMem(1, 10, IntMarshaller, IntMarshaller);
	public final static Table tSession = new TablePer(2, 10, IntMarshaller, new BeanSchemeMarshaller(SessionInfo.STUB));

	static {
		register(tUser);
		register(tSession);
	}
	
	/////////////////////////////////////////////////////////
	//  table wrapper getter methods defines 
	/////////////////////////////////////////////////////////
	
	/*
	 *  		template function. 
	public static <Wrapper> get<Table>(int uid) throws Exception {
		Transaction txn = Transaction.get();
		WKey key = new WKey(tUser, uid);
		WValue value = txn.getData(key);
		if(value != null) {
			Log.trace("getUser from txn. key:%s", key);
			return (<Wrapper>)value.getWrapper();
		} else {
			Log.trace("getUser from table. key:%s", key);
			final TValue v = tUser.get(uid);
			value = new WValue(v);
			<Wrapper> wrap = new <Wrapper>((KeyType)v.getValue(), new WValueNotifier(value));
			value.setWrapper(wrap);
			txn.putData(key, value);
			return wrap;
		}
	}
	 */

	public static WrapperInt getUser(int uid) throws Exception {
		Transaction txn = Transaction.get();
		WKey key = new WKey(tUser, uid);
		WValue value = txn.getData(key);
		if(value != null) {
			Log.info("getUser from txn. key:%s", key);
			return (WrapperInt)value.getWrapper();
		} else {
			Log.info("getUser from table. key:%s", key);
			final TValue v = tUser.get(uid);
			value = new WValue(v);
			WrapperInt wrap = new WrapperInt((Integer)v.getValue(), new WValueNotifier(value));
			value.setWrapper(wrap);
			txn.putData(key, value);
			return wrap;
		}
	}
	
	public static WrapperSessionInfo getSession(int sid) throws Exception {
		Transaction txn = Transaction.get();
		WKey key = new WKey(tSession, sid);
		WValue value = txn.getData(key);
		if(value != null) {
			return (WrapperSessionInfo)value.getWrapper();
		} else {
			final TValue v = tSession.get(sid);
			value = new WValue(v);
			WrapperSessionInfo wrap = new WrapperSessionInfo((SessionInfo)v.getValue(), new WValueNotifier(value));
			value.setWrapper(wrap);
			txn.putData(key, value);
			return wrap;
		}
	}

}
