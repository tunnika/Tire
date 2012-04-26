package util;


import com.avaje.ebean.Ebean;
import com.avaje.ebean.InvalidValue;
import exceptions.FormValidationException;
import play.Logger;
import play.data.Form;
import play.data.validation.ValidationError;
import play.db.ebean.Model;
import play.i18n.Messages;
import play.mvc.Controller;
import util.db.PersistenceErrorHandler;

import javax.persistence.PersistenceException;
import java.util.HashMap;
import java.util.Map;

public class EnhancedController extends Controller {

    /**
     * Handle save form
     * Implements all the validations required to make sure a form gets validated and saved.
     *
     * @param form                  The form to be validated.
     *                              Example: Form<User> userForm = form(User.class).bindFromRequest();
     * @param <T>                   The Model associated with the form (User.class in the given example)
     * @throws exceptions.FormValidationException    In case any validation fails. Error message will be added to the form.
     */
    public static <T> void handleSaveForm(Form<T> form) throws FormValidationException, PersistenceException {
        if(form.hasErrors()){
            throw new FormValidationException();
        } else {
            // Perform javax.persistence validations
            InvalidValue invalid = Ebean.validate(form.get());
            if(invalid==null){
                // database constraints are valid, try to save
                try {
                    ((Model)form.get()).save();
                } catch (PersistenceException pe){
                    // check for exceptions while writing to database
                    // since we may want to override system messages and we cannot evaluate form.get() before
                    // calling form.hasErrors(), let's delegate to the callee handling database errors
                    throw pe;
                } catch (ClassCastException cce){
                    Logger.error("Class wrapped in form is not a Model", cce);
                }
            } else {
                // Database constraints are invalid, log errors
                for(int i=0;i<invalid.getErrors().length;i++){
                    InvalidValue iv = invalid.getErrors()[i];
                    Logger.info("Inconsistent Play and Ebean validations. Validation "
                            + iv.getValidatorKey() + " does not exist in play in field" + iv.getPropertyName());
                }
                // since ebean error codes are not mapped in messages, we better just return a default error to signal we
                // messed things up.
                form.reject(validationError("", Messages.get("db.constraints.failed")));
                throw new FormValidationException();
            }
        }
    }

    /**
     * Make creating a map of code,message a breeze
     * err( "DUPLICATE_PK", "Duplicate email",
     *      "INVALID_FK",   "Invalid currency");
     *
     * Error code should be a member of PersistenceErrorHandler.CODES
     *
     * @param args  Pairs of error code and corresponding message
     * @return      Key value map with error code mapping to its message
     */
    public static Map<PersistenceErrorHandler.CODES, String> err(String... args){
        Map<PersistenceErrorHandler.CODES,String> dictionary = new HashMap<PersistenceErrorHandler.CODES, String>();
        for(int i=0;i<args.length;i+=2){
            try{
                dictionary.put(PersistenceErrorHandler.CODES.valueOf(args[i]), args[i+1]);
            }catch (IllegalArgumentException iae){
                Logger.error("invalid error code:"+args[i]);
            }
        }
        return dictionary;
    }

    /**
     * Creates an instance of ValidationError.
     * @param key       Error key. Empty is handled as a global error
     * @param message   The message itself
     * @return          ValidationError
     */
    public static ValidationError validationError(String key, String message){
        return new ValidationError(key, message, null);
    }

    /**
     * Add global error to a form
     *
     * @param form      the form itself
     * @param message   Message to be added
     * @param <T>       Type the form wraps
     */
    public static <T> void addGlobalError(Form<T> form, String message){
        form.reject(validationError("",message));
    }

    /**
     * Add persistence error to a form
     *
     * @param form      the form itself
     * @param e         the root cause caught in the exception
     * @param <T>       Type the form wraps
     */
    public static <T> void addPersistenceError(Form<T> form, Throwable e){
        addGlobalError(form, PersistenceErrorHandler.getMessageCode(e, null));
    }

    /**
     * Add persistence error to a form
     *
     * @param form      the form itself
     * @param e         the root cause caught in the exception
     * @param messages  messages key/values to override system ones
     * @param <T>       Type the form wraps
     */
    public static <T> void addPersistenceError(Form<T> form, Throwable e, Map<PersistenceErrorHandler.CODES, String> messages){
        // PersistenceErrorHandler parses exceptions and looks for know errors
        //  - returns default message or if it finds it, returns value from param messages.
        //  - otherwise, returns a default message
        addGlobalError(form, PersistenceErrorHandler.getMessageCode(e, messages));
    }
}
