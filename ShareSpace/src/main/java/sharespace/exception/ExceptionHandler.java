package sharespace.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler
    public ResponseEntity<APIResponse> roomException(RoomException e){

        String message=e.getMessage();
        boolean status=false;
        APIResponse response=new APIResponse(message,status);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler
    public ResponseEntity<APIResponse> roommateException(RoommateException e){

        String message=e.getMessage();
        boolean status=false;
        APIResponse response=new APIResponse(message,status);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler
    public ResponseEntity<APIResponse> paymentException(PaymentException e){

        String message=e.getMessage();
        boolean status=false;
        APIResponse response=new APIResponse(message,status);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler
    public ResponseEntity<APIResponse> ownerException(OwnerException e){

        String message=e.getMessage();
        boolean status=false;
        APIResponse response=new APIResponse(message,status);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    @org.springframework.web.bind.annotation.ExceptionHandler
    public ResponseEntity<APIResponse> grievanceException(GrievanceException e){

        String message=e.getMessage();
        boolean status=false;
        APIResponse response=new APIResponse(message,status);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler
    public ResponseEntity<APIResponse> notificationException(NotificationException e){

        String message=e.getMessage();
        boolean status=false;
        APIResponse response=new APIResponse(message,status);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }



}
