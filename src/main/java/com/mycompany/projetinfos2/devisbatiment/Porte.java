/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.projetinfos2.devisbatiment;

/**
 *
 * @author Siméon
 */
public class Porte extends Ouverture {
    private int idPorte;
    public Porte(int id) { this.idPorte = id; }
    @Override public double surface() { return 0.90 * 2.10; }
    @Override public String getNom() { return "Porte"; }
}