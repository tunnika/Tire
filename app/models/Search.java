package models;


import play.data.validation.Constraints;

public class Search {

    @Constraints.Required
    public String searchQuery;
}
