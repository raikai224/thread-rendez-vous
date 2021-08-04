// Raika Iftekhar
// ICS 440
// Due by 6-16-2021 Summer Session

package com.abc.handoff;

import com.abc.pp.stringhandoff.*;
import com.programix.thread.*;
import java.time.*;

public class StringHandoffImpl implements StringHandoff {
	private boolean hasShutdown = false;
	private String message;
	private boolean isPasserActive = false;
	private boolean isReceiverActive = false;
	private boolean isMessageAvailable = false;

    public StringHandoffImpl() {
    }

    @Override
    public synchronized void pass(String msg, long msTimeout)
        throws InterruptedException, TimedOutException, ShutdownException, IllegalStateException {
    	if(!hasShutdown) {
    		
    		if(isPasserActive) {
        		throw new IllegalStateException();
        	}
    		try {
    			isPasserActive = true;
        		getLockObject().notifyAll();
        		
        		
        		if(msTimeout == 0L) {
        			if(hasShutdown) {
                        throw new ShutdownException();
                    }
        			while(!isReceiverActive) {
        				if(hasShutdown) {
                            throw new ShutdownException();
                        }
        				getLockObject().wait();
        			}
        		}else {
        			while(!isReceiverActive) {
            			long timeLeft = msTimeout;
            			long startTime = Instant.now().toEpochMilli() + msTimeout;
            			while (timeLeft > 0L) {
            				if(hasShutdown) {
                                throw new ShutdownException();
                            }
                            getLockObject().wait(timeLeft);
                            if (isReceiverActive) {
                            	break; 
                            }
                            timeLeft = startTime - Instant.now().toEpochMilli();
                        }
            			if(!isReceiverActive) {
            				throw new TimedOutException();
            			}
            		}
        		}
        
        		
        		message = msg;
        		isMessageAvailable = true;
        		getLockObject().notifyAll();
        		
        		while(isMessageAvailable) {
        			getLockObject().wait();
        		}
        		
        		
        	} finally {
        		isPasserActive = false;
        		
        	}
    	}else {
    		throw new ShutdownException();
    	}
    	

    }

    @Override
    public synchronized void pass(String msg) throws InterruptedException, ShutdownException, IllegalStateException {
    	pass(msg, 0);
    }

    @Override
    public synchronized String receive(long msTimeout)
        throws InterruptedException, TimedOutException, ShutdownException, IllegalStateException {
    	if(!hasShutdown) {

    		
    		if(isReceiverActive) {
        		throw new IllegalStateException();
        	}
    		
    		try {
    			isReceiverActive = true;
        		getLockObject().notifyAll();
        		
        		
        		if(msTimeout == 0L) {
        			while(!isPasserActive) {
        				if(hasShutdown) {
                            throw new ShutdownException();
                        }
        				getLockObject().wait();
        			}
        		}else {
        			while(!isPasserActive) {
            			long timeLeft = msTimeout;
            			long startTime = Instant.now().toEpochMilli() + msTimeout;
            			while (timeLeft > 0L) {
            				if(hasShutdown) {
                                throw new ShutdownException();
                            }
                            getLockObject().wait(timeLeft);
                            if (isPasserActive) {
                            	break; 
                            }
                            timeLeft = startTime - Instant.now().toEpochMilli();
                        }
            			
            			if(!isPasserActive) {
            				throw new TimedOutException();
            			}
            		}
        		}
        		
        		
        		
        		while(!isMessageAvailable) {
            		getLockObject().wait();
            	}
        		
            	
            	isMessageAvailable = false;
            	getLockObject().notifyAll();
            	return message;
        		
        	} finally {
        
        		isReceiverActive = false;
        	}
    	}else {
    		throw new ShutdownException();
    	}


    }

    @Override
    public synchronized String receive() throws InterruptedException, ShutdownException, IllegalStateException {
    	
    	
    	return receive(0L);
    }

    @Override
    public synchronized void shutdown() {
    	hasShutdown = true;
        getLockObject().notifyAll();

    }

    @Override
    public Object getLockObject() {
        return this;
    }
}