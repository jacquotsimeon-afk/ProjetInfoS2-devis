/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.projetinfos2.devisbatiment;

/**
 *
 * @author Siméon
 */
public abstract class Revetement {
    protected int idRevetement;
    protected String designation;
    protected double prix;
    protected boolean pourMur, pourSol, pourPlafond;

    public abstract double montant(double surface);
    public int getId() { return idRevetement; }
    public String getDesignation() { return designation; }
    public double getPrix() { return prix; }
    public boolean estPourMur() { return pourMur; }
    public boolean estPourSol() { return pourSol; }
    public boolean estPourPlafond() { return pourPlafond; }
}