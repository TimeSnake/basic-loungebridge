package de.timesnake.basic.loungebridge.util.user;

public class KitNotDefinedException extends Exception {

    private Integer id;

    public KitNotDefinedException(Integer id) {
        this.id = id;
    }

    @Override
    public String getMessage() {
        return "Kit with id " + this.id + " not defined";
    }

    public Integer getId() {
        return id;
    }
}
