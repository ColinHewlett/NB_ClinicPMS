/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.view;

import java.awt.Component;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
/**
 *
 * @author colin
 */
public class AppointmentsTableLocalDateTimeRenderer extends JLabel implements TableCellRenderer{
    private DateTimeFormatter hhmm12Format = DateTimeFormatter.ofPattern("HHmm a");
    
    public AppointmentsTableLocalDateTimeRenderer()
    {
        Font f = super.getFont();
        // bold
        this.setFont(f.deriveFont(f.getStyle() | ~Font.BOLD));;
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
        boolean hasFocus, int row, int column)
    {
        LocalDateTime startTime = (LocalDateTime)value;
        super.setText(startTime.format(hhmm12Format));
        return this;
    }
}
