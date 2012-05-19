package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.validation.constraints.NotNull;

@Entity
public class Notifications {
    public static enum State {
        NEW, LOCKED, SENT, FAILED
    }

    @Id
    public Long id;
    @NotNull
    private String email;
    @NotNull
    private String subject;
    @NotNull
    @Lob
    private String content;
    @NotNull
    private State state = State.NEW;

//-- Queries

    public static Model.Finder<Long, Notifications>
        find = new Model.Finder<Long, Notifications>(Long.class,  Notifications.class);


    public Notifications(){

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
