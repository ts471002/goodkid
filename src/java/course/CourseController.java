package course;

import course.Course;
import entity.util.JsfUtil;
import entity.util.JsfUtil.PersistAction;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import section.Section;
import student.Student;

@Named("courseController")
@SessionScoped
public class CourseController implements Serializable {

    @EJB
    private student.StudentFacade studentFacade;
    @EJB
    private course.CourseFacade ejbFacade;
    private List<Course> items = null;
    private List<Course> itemsbyTeacher = null;
    private Course selected;
    private EntityManager em;
    private Integer id = 1 ;
    private List<Course> items1 =null ;

    public CourseController() {
    }

    public Course getSelected() {
        return selected;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCurrentSectionId(){
        Collection<Section> sections = ejbFacade.find(id).getSectionCollection();
        Iterator<Section> iterator = sections.iterator();
        while (iterator.hasNext()) {
            Section next = iterator.next();
            if (next.getChapterName().startsWith("第一章")) {
                return next.getId();
            }
        }
        return null;
    }
    
    public Course getCourseById() {
        return ejbFacade.find(id);
    }
    
    public void addStudentToCourse(Integer studentId) {
        Student student = studentFacade.find(studentId);
        Course course = ejbFacade.find(id);
        Collection<Course> courses = student.getCourseCollection();
        Collection<Student> students = course.getStudentCollection();
        courses.add(course);
        students.add(student);      // 双向的
        student.setCourseCollection(courses);
        course.setStudentCollection(students);
        studentFacade.edit(student);
        ejbFacade.edit(course);
    }

    public List<Course> getItemsbytype() {
        if (items1 == null) {
            items1 = getFacade().getcoursebytype();
        }
        return items1;
    }

    public void setSelected(Course selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private CourseFacade getFacade() {
        return ejbFacade;
    }

    public Course prepareCreate() {
        selected = new Course();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/Bundle").getString("CourseCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/Bundle").getString("CourseUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/Bundle").getString("CourseDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<Course> getItems() {
        if (items == null) {
            items = getFacade().findAll();
        }
        return items;
    }

    private void persist(PersistAction persistAction, String successMessage) {
        if (selected != null) {
            setEmbeddableKeys();
            try {
                if (persistAction != PersistAction.DELETE) {
                    getFacade().edit(selected);
                } else {
                    getFacade().remove(selected);
                }
                JsfUtil.addSuccessMessage(successMessage);
            } catch (EJBException ex) {
                String msg = "";
                Throwable cause = ex.getCause();
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage(msg);
                } else {
                    JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            }
        }
    }

    public Course getCourse(java.lang.Integer id) {
        return getFacade().find(id);
    }

    public List<Course> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Course> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    @FacesConverter(forClass = Course.class)
    public static class CourseControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            CourseController controller = (CourseController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "courseController");
            return controller.getCourse(getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Integer value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Course) {
                Course o = (Course) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Course.class.getName()});
                return null;
            }
        }

    }

}
