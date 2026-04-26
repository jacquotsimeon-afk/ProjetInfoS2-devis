/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.projetinfos2.devisbatiment;

/**
 *
 * @author Siméon
 */
public class Plafond {
    private Revetement revetement;
    public void appliquerRevetement(Revetement r) { this.revetement = r; }
    public double devis(double s) { return (revetement != null) ? revetement.montant(s) : 0; }
    public Revetement getRevetement() { return revetement; }
}