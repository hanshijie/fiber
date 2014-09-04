package fiber.common;

import java.util.ArrayList;
import java.util.LinkedList;
import fiber.io.Const;
import static fiber.io.Log.log;

public class TaskQueue implements Runnable {
	private final static int task_queue_batch = Const.getProperty("task_queue_batch", 128);
	
	private final LinkedList<Runnable> tasks;
	private final ArrayList<Runnable> waitAddTask;
	
	private boolean running;
	
	public TaskQueue() {
		this.tasks = new LinkedList<Runnable>();
		this.waitAddTask = new ArrayList<Runnable>();
		this.running = false;
	}

	@Override
	public void run() {
		int processedTaskNum = 0;
		while(true) {
			synchronized(this.waitAddTask) {
				this.tasks.addAll(this.waitAddTask);
				this.waitAddTask.clear();
				if(this.tasks.isEmpty()) {
					this.running = false;
					return;
				}
			}
			while(true) {
				Runnable task = this.tasks.poll();
				if(task == null) break;
				try {
					task.run();
				} catch(Exception e) {
					log.error("TaskQuee. task:{}", task, e);
				}
				if(++processedTaskNum >= task_queue_batch) {
					schedule();
					return;
				}
			}
		}
	}
	
	public final void add(Runnable task) {

		synchronized(this.waitAddTask) {
			this.waitAddTask.add(task);
			if(!this.running) {
				this.running = true;
				schedule();
			}
		}
	}
	
	protected void schedule() {
		TaskPool.execute(this);
	}

}
