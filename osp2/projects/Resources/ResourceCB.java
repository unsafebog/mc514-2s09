package osp.Resources;

import java.util.*;
import osp.IFLModules.*;
import osp.Tasks.*;
import osp.Threads.*;
import osp.Utilities.*;
import osp.Memory.*;


public class ResourceCB extends IflResourceCB
{
	RRB rrb;
	static Hashtable request;

	public ResourceCB(int qty)
	{
		super(qty);
	}

	public static void init()
	{
		request = new Hashtable(getSize());
	}

	public RRB do_acquire(int quantity) 
	{
		TaskCB task = MMU.getPTBR().getTask();
		rrb = new RRB(quantity);
		switch(RRB.getReasourceMethod())
		{
			case Avoidance:
				if(quantity + getAllocated() <= getMaxClaim(task.getThread()))//pediu no limite possivel
				{
					if(quantity <= getAvailable())//existe quantidade disponivel
					{
						if(safety(this) == 1)
						{
							grant(this);
							return rrb;
						}
						else
							return null
					
					}
					else//nao existe quantidade disponivel
						return null;
				}
				else//pediu mais que o maximo
					return null;
				break;		
			case Detection:
				if(quantity <= this.getAvailable())
				{
					rrb.grant();
					return rrb;
				}
				else if( quantity + this.getAllocated(task.getCurrentThread()) > this.getTotal() )
					return null;
				else
				{
					rrb.setStatus(Suspended);
					task.getCurrentThread().suspend();
					return rrb;
				}
				break;
		}
		return null;
	}
	//nao conseguimos implementar o sistema de definiçao de estado seguro.
	public int safety(ResourceCB cb)
	{
		
	}

	public static Vector do_deadlockDetection()
	{
	}

	public static void do_giveupResources(ThreadCB thread)
	{
		int qty = this.getAllocated(thread);
	}

	public void do_release(int quantity)
	{
		ThreadCB thread = MMU.getPTBR().getTask().getCurrentThread();
		this.setAllocated(thread, this.getAllocated(thread) - quantity);
		this.setAvailable(this.getAvailable() + quantity);
	}

	public static void atError()
	{

	}

	public static void atWarning()
	{

	}
}

