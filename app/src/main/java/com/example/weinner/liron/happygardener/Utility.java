package com.example.weinner.liron.happygardener;

import java.util.Locale;

/**
 * Created by Liron weinner
 */

class Utility {
    private static final Utility ourInstance = new Utility();

    static Utility getInstance() {
        return ourInstance;
    }

    private Utility() {
    }

    /**
     * Checks if the number got more then 4 digits after decimal point
     * @param number Should be a String
     * @return True if the number got more then 4 digits after decimal point and false otherwise
     */
    private static boolean checkDecimalPoints(final String number){
        boolean status = false;

        String split[]=number.split("\\.");
        try{
            if(split[1].trim().length() > 0){
                status =  true;
            }
        }catch (ArrayIndexOutOfBoundsException e){
            status =  false;
        }

        return status;
    }

    /**
     * Format a number with more then 4 digits after decimal point to a 4 digits after decimal point
     * @param number Should be a String
     * @return The same number if no decimal point or 4 or less digits after point or formatted string with 4 digits after decimal point
     */
    static String setRoundedNumber(final String number){
        // If got more then 4 digits after decimal point
        if(checkDecimalPoints(number)){
            String result = String.format(Locale.getDefault(),"%.4f",Double.valueOf(number)).replaceAll("0*$","");
            int point_index = result.indexOf('.');
            if(point_index != -1){
                if(result.length()-1 == point_index){
                    result = result.substring(0, point_index);
                }
            }
            return result;
        }

        // If got less then 4 digits after decimal point or no decimal point at all
        return number;
    }

}
