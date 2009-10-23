package osp.Resources;


import java.util.*;
import osp.IFLModules.*;
import osp.Tasks.*;
import osp.Threads.*;
import osp.Utilities.*;
import osp.Memory.*;

/**
   The studends module for dealing with resource management. The methods 
   that have to be implemented are do_grant().

   @OSPProject Resources
*/

public class RRB extends IflRRB
{   
	ResourceCB resource;
	ThreadCB thread;
	int status;
	int ID;
	int quantity;
	
	public RRB(ThreadCB th, ResourceCB res , int quant)
	{
		super();
		quantity = quant;
		resource = res;
		thread = th;
	}

	public void do_grant()
	{
		int qty = this.resource.getAllocated(this.thread);
		this.resource.setAvailable(this.resource.getAvailable()-this.quantity);
		this.resource.setAllocated(this.thread, this.quantity+qty);
	}
}
