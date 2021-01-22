/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view.interfaces;
import java.util.HashMap;
import java.time.LocalDate;
/**
 *
 * @author colin
 */
public interface IPatientView {
    public HashMap<String,String> getPatient();
    /*
    public Integer getKey();
    public String getPatientTitle();  //distinguish from JInternalFrame getTitle()
    public String getForenames();
    public String getSurname();
    public String getLine1();
    public String getLine2();
    public String getTown();
    public String getCounty();
    public String getPostcode();
    public String getGender();
    public LocalDate getDOB();
    public Boolean getIsGuardianAPatient();
    public Integer getGuardianKey();
    public Integer getLastDentalAppointmentKey();
    public Integer getNextDentalAppointmentKey();
    public Integer getNextHygieneAppointmentKey();
    public LocalDate getDentalRecallDate();
    public LocalDate getHygieneRecallDate();
    public Integer getDentalRecallFrequency();
    public Integer getHygieneRecallFrequency();
*/
}
