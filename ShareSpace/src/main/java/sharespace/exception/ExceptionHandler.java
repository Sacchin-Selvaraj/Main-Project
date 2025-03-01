package sharespace.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        List<String> fieldname=new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
            fieldname.add(fieldName);
        });
        String message;
        if (fieldname.getFirst().equalsIgnoreCase("username"))
            message=errors.get("username");
        else
           message=errors.get("password");

        boolean status=false;
        APIResponse response=new APIResponse(message,status);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

//    @org.springframework.web.bind.annotation.ExceptionHandler(ConstraintViolationException.class)
//    public ResponseEntity<APIResponse> handleConstraintViolationException(ConstraintViolationException ex) {
//        Map<String, String> errors = new HashMap<>();
//        List<String> fieldname=new ArrayList<>();
//        ex.getConstraintViolations().forEach(violation -> {
//            String fieldName = violation.getPropertyPath().toString();
//            String errorMessage = violation.getMessage();
//            errors.put(fieldName, errorMessage);
//            fieldname.add(fieldName);
//        });
//        String message;
//        if (fieldname.getFirst().equalsIgnoreCase("username"))
//            message=errors.get("username");
//        else
//            message=errors.get("password");
//
//        boolean status=false;
//        APIResponse response=new APIResponse(message,status);
//        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//    }
}