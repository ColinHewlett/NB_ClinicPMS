/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;

import clinicpms.controller.EntityDescriptor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author colin
 */
public class AppointmentsSingleColumnTableModel extends DefaultTableModel{
    private ArrayList<EntityDescriptor.Appointment> appointments = null;
    private enum COLUMN{From};
    private final Class[] columnClass = new Class[] {
        LocalDateTime.class, };
    private String header = null;
    
    public ArrayList<EntityDescriptor.Appointment> getAppointments(){
        return this.appointments;
    }
    
    public AppointmentsSingleColumnTableModel(ArrayList<EntityDescriptor.Appointment> appointments, String header){
        this.appointments = appointments;
        this.header = header;
    }

    @Override
    public int getRowCount(){
        return getAppointments().size();
    }
    @Override
    public int getColumnCount(){
        return COLUMN.values().length;
    }
    @Override
    public String getColumnName(int columnIndex){
        return this.header;
    }
    @Override
    public Class<?> getColumnClass(int columnIndex){
        return columnClass[0];
    }

    @Override
    public Object getValueAt(int row, int column){
        EntityDescriptor.Appointment appointment = getAppointments().get(row);
        return appointment.getData().getStart();
    }
}
