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

	private GenericList threads;
	private GenericList ports;
	private GenericList files;
	
	public TaskCB()
	{
		threads = (ThreadsCB) new GenericList();
		ports = (PortsCB) new GenericList();
		files = (OpenFile) new GenericList();
	}

	
	public static void init()
	{
	

	}

	static public TaskCB do_create()
	{
	

	}

	
	public void do_kill()
	{
	

	}

	
	public int do_getThreadCount()
	{
	

	}

	
	public int do_addThread(ThreadCB thread)
	{
	

	}

	
	public int do_removeThread(ThreadCB thread)
	{
	

	}

	public int do_getPortCount()
	{


	}


	public int do_addPort(PortCB newPort)
	{


	}

	public int do_removePort(PortCB oldPort)
	{


	}

	public void do_addFile(OpenFile file)
	{


	}

	public int do_removeFile(OpenFile file)
	{


	}
	public static void atError()
	{


	}

	public static void atWarning()
	{


	}

		
}
