package org.cru.redegg.it;

import org.apache.log4j.Logger;
import org.cru.redegg.recording.api.ErrorRecorder;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
* @author Matt Drees
*/
@Path("/explosions")
public abstract class AbstractApiThatErrors
{

    @POST
    @Path("throw")
    public void throwBoom()
    {
        recorder().recordUser(new User(42, "joe.staffguy@cru.org"));
        recorder().recordContext("fun fact:", "I'm about to blow");
        throw new IllegalStateException("kablooie!");
    }

    @POST
    @Path("throw")
    @Consumes(MediaType.APPLICATION_JSON)
    /* we don't use the payload, but we need to make sure jax-rs reads it for the test */
    public void throwBoom(String payload)
    {
        this.throwBoom();
    }


    @POST
    @Path("log")
    public void logBoom()
    {
        recorder().recordUser(new User(42, "joe.staffguy@cru.org"));
        recorder().recordContext("fun fact:", "I'm about to blow");
        Logger.getLogger(getClass()).error("kablooie!");
    }

    protected abstract ErrorRecorder recorder();

    public static class User
    {
        int id;
        String name;

        public User(int id, String name)
        {
            this.id = id;
            this.name = name;
        }
    }
}
