/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.projetinfos2.devisbatiment;

/**
 *
 * @author Siméon
 */
public abstract class Batiment { //Super-classe abstraite définissant les bases d'un bâtiment (Maison ou Immeuble).
    protected String idBatiment;
    protected int nbrNiveaux;

    /**
     * Calcule le devis total du bâtiment.
     * Cette méthode sera implémentée différemment par Maison et Immeuble.
     */
    public abstract double devisBatiment();

    /**
     * Calcule la surface au sol totale du bâtiment.
     * Cette méthode sera implémentée différemment par Maison et Immeuble.
     */
    public abstract double surfaceSolBatiment();

    /**
     * Retourne l'identifiant (ou le nom) du bâtiment.
     */
    public String getId() { 
        return idBatiment; 
    }
}