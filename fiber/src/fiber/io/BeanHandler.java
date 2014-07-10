package fiber.io;

/**
 * bean处理器的基类(抽象类)
 */
public abstract class BeanHandler<A extends Bean<A>>
{
	/**
	 * 处理的入口
	 */
	@SuppressWarnings("unchecked")
	final void process(IOSession session, Bean<?> bean) throws Exception
	{
		onProcess(session, (A)bean);
	}

	/**
	 * 处理回调的接口
	 */
	public abstract void onProcess(IOSession session, A arg) throws Exception;
}
