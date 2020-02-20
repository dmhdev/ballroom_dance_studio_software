/*
 * Several common methods employed by different views and classes. Mostly used to populate JTables and JComboBoxes with data.
 */
package commons.helpers;

/**
 *
 * @author daynehammes
 */
public class ValidationHelper {
      
    ServerHelper server = new ServerHelper();

    
    public boolean isValidVarChar(String inputID, int sqlColumnLength) {
        
        if ( (inputID.length() > sqlColumnLength) ) {
            return false;
        }
        
        return true;
        
    }
    
    public boolean isValidNumeric(String inputNumber) {
        
        if (inputNumber.trim().equals("")) {
            return true;
        }
        
        try {
            double d = Double.parseDouble(inputNumber);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
    
}
