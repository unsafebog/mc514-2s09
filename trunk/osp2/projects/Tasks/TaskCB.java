package osp.Tasks;

import java.util.Vector;
import osp.IFLModules.*;
import osp.Threads.*;
import osp.Ports.*;
import osp.Memory.*;
import osp.FileSys.*;
import osp.Utilities.*;
import osp.Hardware.*;


public class TaskCB extends IflTaskCB
{

	static private GenericList threads;
	static private GenericList ports;
	static private GenericList files;
	static private TaskCB newTask;
	
	public TaskCB()
	{
		super();
		
	}

	
	public static void init()
	{
	

	}

	static public TaskCB do_create()
	{
		//create task object
		newTask = new TaskCB();
		
		//Create page table
		PageTable newPageTable = new PageTable(newTask);
		//Set Ttask's pagetable
		newTask.setPageTable(newPageTable);
		
		//Set task' status
		newTask.setStatus(TaskLive);
		
		//Set creation time
		newTask.setCreationTime(HClock.get());
		
		//Creates swap file
		String SwapPathName = SwapDeviceMountPoint;
		SwapPathName.concat(Integer.toString(newTask.getID()));
		int createdSwapFile = FileSys.create(SwapPathName, MMU.getVirtualAddressBits());
		//  AQUI FAZER O ERROR ****************************************************************
		
		//Open swap file
		OpenFile swap = OpenFile.open(SwapDeviceMountPoint, newTask);
		newTask.setSwapFile(swap);
		//  AQUI FAZER O ERROR ****************************************************************
		
		threads.insert(ThreadCB.create(newTask));
		
		return newTask;
	}

	
	public void do_kill()
	{
		Enumeration remove = threads.forwardIterator();
		while(remove.hasMoreElements())
		{
			Object obj = remove.nextElement();
			newTask.do_remove(obj);
		}
		
	}

	
	public int do_getThreadCount()
	{
		
		return 0;

	}

	
	public int do_addThread(ThreadCB thread)
	{
		return 0;

	}

	
	public int do_removeThread(ThreadCB thread)
	{
		
		if(threads.remove(thread) != null)
			return 1;
		else
			return 0;

	}

	public int do_getPortCount()
	{
		return 0;

	}


	public int do_addPort(PortCB newPort)
	{
		return 0;

	}

	public int do_removePort(PortCB oldPort)
	{
		return 0;

	}

	public void do_addFile(OpenFile file)
	{


	}

	public int do_removeFile(OpenFile file)
	{
		return 0;

	}
	public static void atError()
	{
		System.out.println("oi");

	}

	public static void atWarning()
	{


	}

	
}
