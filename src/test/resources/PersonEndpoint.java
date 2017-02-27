package swagger;
import javax.ws.rs.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;


@Path("/people")
public class PersonEndpoint {

   @GET
   public Response getPerson() {
      return Response.ok().build();
     }


}