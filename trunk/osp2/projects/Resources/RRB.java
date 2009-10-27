package osp.Resources;

import java.util.*;

import osp.Utilities.*;
import osp.IFLModules.*;
import osp.Threads.*;

public class RRB extends IflRRB
{
	int id;
	ThreadCB thread;
	ResourceCB resource;
	int quantity;
	int status;
	

	public RRB(ThreadCB th, ResourceCB res ,int qty)
	{
		super(th, res, qty);
		id = this.getID();
		thread = th;
		resource = res;
		quantity = qty;
		status = this.getStatus();
	}

	public void do_grant()
	{
		this.resource.setAvailable(this.resource.getAvailable() - this.quantity);
		this.resource.setAllocated(this.thread,
			this.resource.getAllocated(this.thread) + this.quantity);
			
		this.setStatus(Granted);
		this.status = this.getStatus();
		
		this.notifyThreads();
	}


    /*
       Feel free to add methods/fields to improve the readability of your code
    */

}

/*
      Feel free to add local classes to improve the readability of your code
*/
