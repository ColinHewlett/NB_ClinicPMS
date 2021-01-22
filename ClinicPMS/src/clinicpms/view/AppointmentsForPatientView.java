/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;

import clinicpms.controller.ViewController;
import clinicpms.view.interfaces.IView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import javax.swing.JInternalFrame;

/**
 *
 * @author colin
 */
public class AppointmentsForPatientView extends JInternalFrame
                                        implements PropertyChangeListener,
                                                   IView{
    private ActionListener myController = null;
    private HashMap<String,String> entity = null;
    
    public AppointmentsForPatientView(ActionListener myController){
        setMyController(myController);
        
    }

    public HashMap<String,String> getEntity(){
        return this.entity;
    }
    public void initialiseView(){
        
    }
    
    public void propertyChange(PropertyChangeEvent e){
        
    }
    
    private void setMyController(ActionListener myController){
        this.myController = myController;
    }
    
}
