/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinicpms.store;

import clinicpms.model.Appointment;
import clinicpms.model.Patient;
import clinicpms.store.exceptions.StoreException; 
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.Comparator;

/**
 *
 * @author colin
 */
public class CSVStore extends Store {

    private static CSVStore instance;
    private BufferedReader appointmentReader = null;
    private BufferedReader patientReader = null;
    private CSVReader csvAppointmentsReader = null;
    private CSVReader csvPatientsReader = null;
    private CSVWriter csvAppointmentsWriter = null;
    private CSVWriter csvPatientsWriter = null;
    
    private enum AppointmentField {KEY,
                                   PATIENT,
                                   START,
                                   DURATION,
                                   NOTES}
    
    private enum PatientField {KEY,
                              TITLE,
                              FORENAMES,
                              SURNAME,
                              LINE1,
                              LINE2,
                              TOWN,
                              COUNTY,
                              POSTCODE,
                              PHONE1,
                              PHONE2,
                              GENDER,
                              DOB,
                              IS_GUARDIAN_A_PATIENT,
                              GUARDIAN,
                              NOTES,
                              DENTAL_DATE,
                              HYGIENE_DATE,
                              DENTAL_FREQUENCY,
                              HYGIENE_FREQUENCY}
    
    public enum ExceptionType {IO_EXCEPTION,
                                 CSV_EXCEPTION,
                                 NULL_KEY_EXPECTED_EXCEPTION,
                                 NULL_KEY_EXCEPTION,
                                 INVALID_KEY_VALUE_EXCEPTION,
                                 KEY_NOT_FOUND_EXCEPTION}
    
    public final Path DATABASE_FOLDER = Paths.get(
     "C:\\Users\\colin\\OneDrive\\Documents\\NetBeansProjects\\ClinicPMS\\data\\csv\\");
    public static final String APPOINTMENTS_FILE = "appointments.csv";
    public static final String PATIENTS_FILE = "patients.csv";

    public static CSVStore getInstance() throws StoreException{
        CSVStore result = null;
        if (instance == null){
            result = new CSVStore();
        }
        return result;
    }
    /**
     * Constructs two new CSVReader objects, one to read from appointments.csv 
     * file and one to read from patients.csv file.
     * @throws StoreException
     */
    public CSVStore() throws StoreException{ 
        Path appointmentsPath = DATABASE_FOLDER.resolve(APPOINTMENTS_FILE);
        Path patientsPath = DATABASE_FOLDER.resolve(PATIENTS_FILE);
        try{
            File file = new File(appointmentsPath.toString());
            file.createNewFile();
            appointmentReader = Files.newBufferedReader(appointmentsPath);
            csvAppointmentsReader = new CSVReader(appointmentReader);
        }
        catch (IOException e){
            throw new StoreException(e.getMessage(),ExceptionType.IO_EXCEPTION);
        }

        try{
            File file = new File(patientsPath.toString());
            file.createNewFile();
            patientReader = Files.newBufferedReader(patientsPath);
            csvPatientsReader = new CSVReader(patientReader);
        }
        catch (IOException e){
            String message = "IOException message -> " + e.getMessage() + "\n" +
                    "StoreException message -> Error encountered in CSVStore constructor " +
                    "on initialisation of patientReader File object";
            throw new StoreException(message, ExceptionType.IO_EXCEPTION);
        }
        
        File patientFile = new File(patientsPath.toString());
        try{
            FileWriter outputPatientFile = new FileWriter(patientFile);
            csvPatientsWriter = new CSVWriter(outputPatientFile);
        }
        catch (IOException e){
            throw new StoreException(e.getMessage(), ExceptionType.IO_EXCEPTION);
        }
        
        File appointmentFile = new File(appointmentsPath.toString());
        try{
            FileWriter outputAppointmentsFile = new FileWriter(appointmentFile);
            csvPatientsWriter = new CSVWriter(outputAppointmentsFile);
        }
        catch (IOException e){ 
            throw new StoreException(e.getMessage(), ExceptionType.IO_EXCEPTION);
        }  
    }
    
    /**
     * Creates a unique key for the received Patient object and adds the 
     * serialised Patient record to the patients csv file
     * @param p Patient with a null key value
     * @throws StoreException if the received \Patient object does not have a 
     * null key
     */
    @Override
    public Patient create(Patient p)   throws StoreException{
        Patient result = null;
        List<String[]> readPatientsStringArrayList;
        if (p.getKey() == null){
            readPatientsStringArrayList = readPatientsAsStringArrayList();
            Integer nextPatientKey = getNextHighestKeyFromRecords(readPatientsStringArrayList);
            p.setKey(nextPatientKey);
            String[] serialisedPatient = serialise(p);
            this.csvPatientsWriter.writeNext(serialisedPatient);
            return read(p);
        }
        else{
            throw new StoreException(
                    "Received patient key not null although"
                       + "expected null by CSVStore.create(Patient p) method",
                    ExceptionType.NULL_KEY_EXPECTED_EXCEPTION);
        }
    }
    
    /**
     * Creates a unique key for the received Appointment object and adds the 
     * serialised Appointment to the patients csv file
     * @param a Appointment with a null key value
     * @throws StoreException if the received Appointment object does not have a 
     * null key
     * @return Appointment, reads back from store the newly created appointment
     */
    @Override
    public Appointment create(Appointment a)   throws StoreException{
        List<String[]> readAppointmentsStringArrayList;
        if (a.getKey() == null){
            readAppointmentsStringArrayList = readAppointmentsAsStringArrayList();
            Integer nextAppointmentKey = getNextHighestKeyFromRecords(readAppointmentsStringArrayList);
            a.setKey(nextAppointmentKey);
            String[] serialisedAppointment = serialise(a);
            this.csvAppointmentsWriter.writeNext(serialisedAppointment);
            return read(a);
        }
        else{
            throw new StoreException(
                    "Received appointment key not null although"
                       + "expected null by CSVStore.create(Appointment a) method",
                    ExceptionType.NULL_KEY_EXPECTED_EXCEPTION);
        }
    }

    /**
     * Calculates the next highest (new) key from the records received
     * @param records List<String[]>
     * @return Integer representing the next available key for the record
     * collection
     */
    public Integer getNextHighestKeyFromRecords(List<String[]> records){
        Iterator<String[]> it = records.iterator();
        ArrayList<Integer> keys = new ArrayList<>();
        while (it.hasNext()){
            String[] s = it.next();
            keys.add(Integer.parseInt(s[PatientField.KEY.ordinal()]));
        }
        Collections.sort(keys);
        return keys.get(keys.size()-1) + 1;
    }
    
    /**
     * 
     * @param a Appointment object to serialise
     * @return String[] result of the serialisation
     */
    private String[] serialise(Appointment a){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String[] sA = new String[AppointmentField.values().length];
        for (AppointmentField af: AppointmentField.values()){
            switch(af){
                case KEY -> 
                    sA[AppointmentField.KEY.ordinal()] = a.getKey().toString();
                case PATIENT -> 
                    sA[AppointmentField.PATIENT.ordinal()] = a.getKey().toString();
                case START ->
                    sA[AppointmentField.START.ordinal()] = 
                            a.getStart().format(formatter);
                case DURATION ->
                    sA[AppointmentField.DURATION.ordinal()] = 
                            Long.toString(a.getDuration().toMinutes());
                case NOTES ->
                    sA[AppointmentField.NOTES.ordinal()] = a.getNotes();       
            }
        }
        return sA;
    }
    
    private String[] serialise(Patient p){
        DateTimeFormatter dobFormat = DateTimeFormatter.ofPattern("dd/mm/yyyy");
        DateTimeFormatter recallFormat = DateTimeFormatter.ofPattern("MM/yy");
        String[] sP = new String[PatientField.values().length];
        
        for (PatientField pf: PatientField.values()){
            switch(pf){
                case KEY -> sP[PatientField.KEY.ordinal()] = 
                        p.getKey().toString();
                case TITLE -> sP[PatientField.TITLE.ordinal()] = 
                        p.getName().getTitle();
                case FORENAMES -> sP[PatientField.FORENAMES.ordinal()] = 
                        p.getName().getForenames();
                case SURNAME -> sP[PatientField.SURNAME.ordinal()] = 
                        p.getName().getSurname();
                case LINE1 -> sP[PatientField.LINE1.ordinal()] = 
                        p.getAddress().getLine1();
                case LINE2 -> sP[PatientField.LINE2.ordinal()] = 
                        p.getAddress().getLine2();
                case TOWN -> sP[PatientField.TOWN.ordinal()] = 
                        p.getAddress().getTown();
                case COUNTY -> sP[PatientField.COUNTY.ordinal()] = 
                        p.getAddress().getCounty();
                case POSTCODE -> sP[PatientField.POSTCODE.ordinal()] = 
                        p.getAddress().getPostcode();
                case DENTAL_DATE -> sP[PatientField.DENTAL_DATE.ordinal()] = 
                        p.getRecall().getDentalDate().format(recallFormat);
                case HYGIENE_DATE -> sP[PatientField.HYGIENE_DATE.ordinal()] = 
                        p.getRecall().getHygieneDate().format(recallFormat);
                case DENTAL_FREQUENCY -> sP[PatientField.DENTAL_FREQUENCY.ordinal()] = 
                        p.getRecall().getDentalFrequency().toString();
                case HYGIENE_FREQUENCY -> sP[PatientField.HYGIENE_FREQUENCY.ordinal()] = 
                        p.getRecall().getDentalFrequency().toString();
                case DOB -> sP[PatientField.DOB.ordinal()] = 
                        p.getDOB().format(dobFormat);
                case GENDER -> sP[PatientField.GENDER.ordinal()] = 
                        p.getGender();
                case IS_GUARDIAN_A_PATIENT -> sP[PatientField.IS_GUARDIAN_A_PATIENT.ordinal()] = 
                        Boolean.toString(p.getIsGuardianAPatient());
                case GUARDIAN -> sP[PatientField.GUARDIAN.ordinal()] = 
                        String.valueOf(p.getGuardian());
                case NOTES -> sP[PatientField.NOTES.ordinal()] = 
                        p.getGuardian().toString();
            }
        }
        return sP;
    }
    
    private List<String[]> readPatientsAsStringArrayList() throws StoreException{
        try{
            List<String[]> data = this.csvPatientsReader.readAll();
            return data;
        }
        catch (IOException e){
            throw new StoreException(e.getMessage(), ExceptionType.IO_EXCEPTION);
        }
        catch (CsvException e){
            throw new StoreException(e.getMessage(), ExceptionType.CSV_EXCEPTION);
        }  
    }
    
    private List<String[]> readAppointmentsAsStringArrayList() throws StoreException{
        try{
            List<String[]> data = this.csvAppointmentsReader.readAll();
            return data;
        }
        catch (IOException e){
            throw new StoreException(e.getMessage(), ExceptionType.IO_EXCEPTION);
        }
        catch (CsvException e){
            throw new StoreException(e.getMessage(), ExceptionType.CSV_EXCEPTION);
        }  
    }
    
    public ArrayList<Patient> readPatients() throws StoreException{
        ArrayList<Patient> patients = new ArrayList<>();
        List<String[]> patientsStringArrayList = readPatientsAsStringArrayList();
        Iterator<String[]> it = patientsStringArrayList.iterator();
        while (it.hasNext()){
            String[] patientStringArray = it.next();
            Patient p = makePatientFromStringArray(patientStringArray);
            patients.add(p);
        }
        sortPatientsByName(patients);
        return patients;
    }
    /**
     * 
     * @return ArrayList<Appointment> time ordered collection of appointments
     * @throws StoreException
     */
    public ArrayList<Appointment> readAppointments() throws StoreException{
        ArrayList<Appointment> appointments = new ArrayList<>();
        List<String[]> appointmentsStringArrayList = readAppointmentsAsStringArrayList();
        Iterator<String[]> it = appointmentsStringArrayList.iterator();
        while (it.hasNext()){
            String[] appointmentStringArray = it.next();
            Appointment a = makeAppointmentFromStringArray(appointmentStringArray);
            appointments.add(a);
        }
        sortAppointmentsByDay(appointments);
        return appointments;
    }
    
    /**
     * 
     * @param day
     * @return ArrayList of Appointment objects time ordered list of appointments for the
     * specified day
     * @throws StoreException
     */
    @Override
    public ArrayList<Appointment> readAppointments(LocalDate day) throws StoreException{
        ArrayList<Appointment> dayAppointments = new ArrayList<>();
        ArrayList<Appointment> appointments = readAppointments();
        Iterator<Appointment> it = appointments.iterator();
        while (it.hasNext()){
            Appointment a = it.next();
            if (a.getStart().toLocalDate().equals(day)){
                dayAppointments.add(a);
            } 
        }
        return dayAppointments;
    }
    
    /**
     * 
     * @param p Patient object
     * @param t Category enumerator constant  
     * @return ArrayList of Appointment objects time ordered for this patient
     * @throws StoreException
     */
    @Override
    public ArrayList<Appointment> readAppointments(Patient p, Appointment.Category t) throws StoreException{
        ArrayList<Appointment> patientAppointments = new ArrayList<>();
        ArrayList<Appointment> appointments = readAppointments();
        Iterator<Appointment> it = appointments.iterator();
        while (it.hasNext()){
            Appointment a = it.next();
            
            if (a.getPatient().getKey().equals(p.getKey())){
                switch (a.getCategory()){
                    case DENTAL -> {
                        if (t.equals(Appointment.Category.DENTAL)){
                            patientAppointments.add(a);
                        }
                    }
                    case HYGIENE -> {
                        if (t.equals(Appointment.Category.HYGIENE)){
                            patientAppointments.add(a);
                        }
                    }
                    case ALL -> patientAppointments.add(a);
                }
            }
        }
        return patientAppointments;
    }
    
    /**
     * 
     * object
     * @param appointmentRecord
     * @return Appointment, which represents a String[] 
     * @throws StoreException 
     */
    private Appointment makeAppointmentFromStringArray(String[] appointmentRecord) throws StoreException{
        DateTimeFormatter startFormat = DateTimeFormatter.ofPattern("dd/mm/yyyy hh:mm");
        Appointment a = new Appointment();
        for (AppointmentField af: AppointmentField.values()){
            if (af.equals(AppointmentField.PATIENT)){
                Patient patient = read(new Patient(Integer.parseInt(
                        appointmentRecord[AppointmentField.KEY.ordinal()])));
                a.setPatient(patient);
            }
            else{
                switch (af){
                    case KEY -> a.setKey(Integer.parseInt(
                                        appointmentRecord[AppointmentField.KEY.ordinal()]));
                    case START -> a.setStart(LocalDateTime.parse(
                            appointmentRecord[AppointmentField.START.ordinal()],startFormat));
                    case DURATION -> a.setDuration(Duration.ofMinutes(Long.parseLong(
                                appointmentRecord[AppointmentField.START.ordinal()])));
                    case NOTES -> a.setNotes(
                            appointmentRecord[AppointmentField.NOTES.ordinal()]);
                }
            }
        }
        return a;
    }
    
    /**
     * Converts a row read from the patient csv file to a Patient. The method 
     * can be called recursively if this patient has a guardian who is also a 
     * patient. The recursion halts when  the patient field 
     * Is_Guardian_A_Patient is false. Successful processing relies therefor
     * that the Is_Guardian_A_Patient patient field is processed before the 
     * Guardian field. Scope for a business rule here -> this field can only be 
     * true if patient age is less than 18; which would make it extremely 
     * unlikely (impossible?)for there to be than a single recursive call on the 
     * method
     * @param patientRecord, which represents a String[]
     * @return Patient
     * @throws StoreException 
     */
    private Patient makePatientFromStringArray(String[] patientRecord) throws StoreException{
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/mm/yyyy");
        Patient p = new Patient();
        for (PatientField pf: PatientField.values()){
            switch (pf){
                case KEY -> p.setKey(Integer.parseInt(
                                    patientRecord[PatientField.KEY.ordinal()]));
                case TITLE -> p.getName().setTitle(
                                    patientRecord[PatientField.TITLE.ordinal()]);
                case FORENAMES -> p.getName().setForenames(
                            patientRecord[PatientField.FORENAMES.ordinal()]);
                case SURNAME -> p.getName().setSurname(
                            patientRecord[PatientField.SURNAME.ordinal()]);
                case LINE1 -> p.getAddress().setLine1(
                            patientRecord[PatientField.LINE1.ordinal()]);
                case LINE2 -> p.getAddress().setLine2(
                            patientRecord[PatientField.LINE2.ordinal()]);
                case TOWN -> p.getAddress().setTown(
                            patientRecord[PatientField.TOWN.ordinal()]);
                case COUNTY -> p.getAddress().setCounty(
                            patientRecord[PatientField.COUNTY.ordinal()]);
                case POSTCODE -> p.getAddress().setPostcode(
                            patientRecord[PatientField.POSTCODE.ordinal()]);
                case DENTAL_DATE -> p.getRecall().setDentalDate(LocalDate.parse(
                    patientRecord[PatientField.DENTAL_DATE.ordinal()],dateFormat));
                case HYGIENE_DATE -> p.getRecall().setHygieneDate(LocalDate.parse(
                    patientRecord[PatientField.HYGIENE_DATE.ordinal()],dateFormat));
                case HYGIENE_FREQUENCY -> p.getRecall().setHygieneFrequency(Integer.parseInt(
                    patientRecord[PatientField.HYGIENE_FREQUENCY.ordinal()]));
                case DENTAL_FREQUENCY -> p.getRecall().setDentalFrequency(Integer.parseInt(
                    patientRecord[PatientField.DENTAL_FREQUENCY.ordinal()]));
                case GENDER -> p.setGender(
                        patientRecord[PatientField.GENDER.ordinal()]);
                case PHONE1 -> p.setPhone1(
                        patientRecord[PatientField.PHONE1.ordinal()]);
                case PHONE2 -> p.setPhone2(
                        patientRecord[PatientField.PHONE2.ordinal()]);
                case DOB -> p.setDOB(LocalDate.parse(
                        patientRecord[PatientField.DOB.ordinal()],dateFormat));
                case NOTES -> p.setNotes(
                        patientRecord[PatientField.NOTES.ordinal()]);
                case IS_GUARDIAN_A_PATIENT -> p.setIsGuardianAPatient(Boolean.valueOf(
                        patientRecord[PatientField.IS_GUARDIAN_A_PATIENT.ordinal()]));

            }
            if (pf.equals(PatientField.GUARDIAN)){
                if (p.getIsGuardianAPatient()){
                    Integer key = (
                            patientRecord[PatientField.KEY.ordinal()] == null) ? 
                            null:Integer.parseInt(
                                    patientRecord[PatientField.KEY.ordinal()]);
                    if (key!=null){
                        Patient guardian = new Patient(key);
                        p.setGuardian(read(guardian));
                    } 
                }
            }
        }
        return p;
    }

    /**
     * The key field is the only field of the Appointment parameter considered, 
     * and is used to read the corresponding appointment record from the database.
     * @param a represents an Appointment object
     * @return Appointment
     * @throws StoreException 
     */
    @Override
    public Appointment read(Appointment a) throws StoreException{ 
        Appointment appointment = null;
        Integer key = a.getKey();
        if (key == null){
            throw new StoreException("Null key specified in call to "
                                        + "CSVStore.read(Appointment) method",
                                        ExceptionType.NULL_KEY_EXCEPTION);
        }
        else if (key < 0){
            throw new StoreException("Negative key value specified "
                                                + "in call to "
                                                + "CSVStore.update(Appointment) "
                                                + "method",
                                                ExceptionType.INVALID_KEY_VALUE_EXCEPTION);
        }
        else {
            List<String[]> appointmentsStringArrayList = readAppointmentsAsStringArrayList();
            Iterator<String[]> it = appointmentsStringArrayList.iterator();
            while (it.hasNext()){
                String[] appointmentStringArray = it.next();
                if (appointmentStringArray[AppointmentField.KEY.ordinal()].
                                            equals(String.valueOf(a.getKey()))){
                    appointment = makeAppointmentFromStringArray(appointmentStringArray);
                }
            }      
        }
        return appointment;
    }
    
    /**
     * The key field is the only field of the Patient parameter considered, 
     * and is used to read the corresponding appointment record from the database.
     * @param p represents a Patient object
     * @return Patient
     * @throws StoreException 
     */
    @Override
    public Patient read(Patient p)throws StoreException{
        Patient patient = null;
        Integer key = p.getKey();
        if (key == null){
            throw new StoreException("Null key specified in call to "
                                        + "CSVStore.read(Patient) method",
                                        ExceptionType.NULL_KEY_EXCEPTION);
        }
        else if (key < 0){
            throw new StoreException("Negative key value specified "
                                                + "in call to "
                                                + "CSVStore.update(Patient) "
                                                + "method", 
                                                ExceptionType.INVALID_KEY_VALUE_EXCEPTION);
        }
        else {
            List<String[]> patientsStringArrayList = readPatientsAsStringArrayList();
            Iterator<String[]> it = patientsStringArrayList.iterator();
            while (it.hasNext()){
                String[] patientStringArray = it.next();
                if (patientStringArray[PatientField.KEY.ordinal()].
                        equals(String.valueOf(p.getKey()))){
                    patient = makePatientFromStringArray(patientStringArray);
                }
            }      
        }
        return patient;
    }
     
    @Override
    public Appointment update(Appointment a) throws StoreException{
        boolean isAppointmentRecordFound = false;
        Integer key = a.getKey();
        if (key == null){
            throw new StoreException("Null key specified in call to "
                                        + "CSVStore.update(Appointment) method",
                                        ExceptionType.NULL_KEY_EXCEPTION);
        }
        else if (key < 0){
            throw new StoreException("Negative key value specified "
                                                + "in call to "
                                                + "CSVStore.update(Appointment) "
                                                + "method",
                                                ExceptionType.INVALID_KEY_VALUE_EXCEPTION);
        }
        else {
            List<String[]> appointmentsStringArrayList = readAppointmentsAsStringArrayList();
            Iterator<String[]> it = appointmentsStringArrayList.iterator();
            int index = -1;
            while (it.hasNext()){
                index++;
                String[] appointmentStringArray = it.next();
                if (appointmentStringArray[AppointmentField.KEY.ordinal()].
                        equals(String.valueOf(a.getKey()))){
                    isAppointmentRecordFound = true;
                    break;
                }
            }
            if (!isAppointmentRecordFound){
                throw new StoreException("Specified appointment key in "
                            + "CSVStore.Update(Appointment) not found in darabase.",
                                        ExceptionType.KEY_NOT_FOUND_EXCEPTION);
            }
            else{
                appointmentsStringArrayList.remove(index);
                this.csvAppointmentsWriter.writeAll(appointmentsStringArrayList);
                String[] serialisedAppointment = serialise(a);
                this.csvAppointmentsWriter.writeNext(serialisedAppointment);
                return read(a);
            }
        }
        
    }
    
    @Override
    public Patient update(Patient p) throws StoreException{
        boolean isPatientRecordFound = false;
        Integer key = p.getKey();
        if (key == null){
            throw new StoreException("Null key specified in call to "
                                        + "CSVStore.update(Patient) method",
                                        ExceptionType.NULL_KEY_EXCEPTION);
        }
        else if (key < 0){
            throw new StoreException("Negative key value specified "
                                                + "in call to "
                                                + "CSVStore.update(Patient) "
                                                + "method",
                                                ExceptionType.INVALID_KEY_VALUE_EXCEPTION);
        }
        else {
            List<String[]> patientsStringArrayList = readPatientsAsStringArrayList();
            Iterator<String[]> it = patientsStringArrayList.iterator();
            int index = -1;
            while (it.hasNext()){
                index++;
                String[] patientStringArray = it.next();
                if (patientStringArray[PatientField.KEY.ordinal()].
                        equals(String.valueOf(p.getKey()))){
                    isPatientRecordFound = true;
                    break;
                }
            }
            if (!isPatientRecordFound){
                throw new StoreException("Specified patient key in "
                            + "CSVStore.Update(Patient) not found in darabase.",
                                        ExceptionType.KEY_NOT_FOUND_EXCEPTION);
            }
            else{
                patientsStringArrayList.remove(index);
                this.csvPatientsWriter.writeAll(patientsStringArrayList);
                String[] serialisedPatient = serialise(p);
                this.csvPatientsWriter.writeNext(serialisedPatient);
                return read(p);
            }
        }
    }

    @Override
    public void delete(Appointment a) throws StoreException{
        boolean isAppointmentRecordFound = false;
        Integer key = a.getKey();
        if (key == null){
            throw new StoreException("Key value null in call to "
                                        + "CSVStore.delete(Appointment) method",
                                        ExceptionType.NULL_KEY_EXCEPTION);
        }
        else if (key < 0){
            throw new StoreException("Specified key value not found in "
                                      + "call to CSVStore.delete(Appointment) "
                                      + "method",
                                      ExceptionType.KEY_NOT_FOUND_EXCEPTION);
        }
        else {
            List<String[]> appointmentsStringArrayList = readAppointmentsAsStringArrayList();
            Iterator<String[]> it = appointmentsStringArrayList.iterator();
            int index = -1;
            while (it.hasNext()){
                index++;
                String[] appointmentStringArray = it.next();
                if (appointmentStringArray[AppointmentField.KEY.ordinal()].
                        equals(String.valueOf(a.getKey()))){
                    isAppointmentRecordFound = true;
                    break;
                }
            }
            if (!isAppointmentRecordFound){
                throw new StoreException("Specified appointment key in "
                            + "CSVStore.delete(Appointment) not found in database.",
                                        ExceptionType.KEY_NOT_FOUND_EXCEPTION);
            }
            else{
                appointmentsStringArrayList.remove(index);
                this.csvAppointmentsWriter.writeAll(appointmentsStringArrayList);
            }
        }
    }
    
    @Override
    public void delete(Patient p) throws StoreException{
        boolean isPatientRecordFound = false;
        Integer key = p.getKey();
        if (key == null){
            throw new StoreException("Null key specified in call to "
                                        + "CSVStore.delete(Patient) method",
                                        ExceptionType.NULL_KEY_EXCEPTION);
        }
        else if (key < 0){
            throw new StoreException("Negative key value specified "
                                                + "in call to "
                                                + "CSVStore.delete(Patient) "
                                                + "method",
                                                ExceptionType.INVALID_KEY_VALUE_EXCEPTION);
        }
        else {
            List<String[]> patientsStringArrayList = readPatientsAsStringArrayList();
            Iterator<String[]> it = patientsStringArrayList.iterator();
            int index = -1;
            while (it.hasNext()){
                index++;
                String[] patientStringArray = it.next();
                if (patientStringArray[PatientField.KEY.ordinal()].
                        equals(String.valueOf(p.getKey()))){
                    isPatientRecordFound = true;
                    break;
                }
            }
            if (!isPatientRecordFound){
                throw new StoreException("Specified patient key in "
                            + "CSVStore.delete(Patient) not found in database.",
                                        ExceptionType.KEY_NOT_FOUND_EXCEPTION);
            }
            else{
                patientsStringArrayList.remove(index);
                this.csvPatientsWriter.writeAll(patientsStringArrayList);
            }
        }
    }
    
    private void sortAppointmentsByDay(ArrayList<Appointment> unSortedA){
        Comparator<Appointment> compareByStart
                = (Appointment o1, Appointment o2)
                -> o1.getStart().compareTo(o2.getStart());
        Collections.sort(unSortedA, compareByStart);
    }
    
    private void sortPatientsByName(ArrayList<Patient> patients){
        Comparator<Patient> compareByName
                = (Patient o1, Patient o2)
                -> (o1.getName().getSurname() + o1.getName().getForenames()).
                        compareTo(o2.getName().getSurname() + o2.getName().getForenames());
        Collections.sort(patients, compareByName);
    }
}
