/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.LocalDateTime;

/**
 *
 * @author cnvtte
 */
public class Ticket {
    private int id;
    private String placaVeiculo;
    private LocalDateTime horarioEntrada;
    private Vaga vaga; //relaciona com a vaga

    public Ticket(){}
    
    public Ticket(int id, String placaVeiculo, LocalDateTime horarioEntrada, Vaga vaga){
        this.id = id;
        this.placaVeiculo = placaVeiculo;
        this.horarioEntrada = horarioEntrada;
        this.vaga = vaga;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlacaVeiculo() {
        return placaVeiculo;
    }

    public void setPlacaVeiculo(String placaVeiculo) {
        this.placaVeiculo = placaVeiculo;
    }

    public LocalDateTime getHorarioEntrada() {
        return horarioEntrada;
    }

    public void setHorarioEntrada(LocalDateTime horarioEntrada) {
        this.horarioEntrada = horarioEntrada;
    }

    public Vaga getVaga() {
        return vaga;
    }

    public void setVaga(Vaga vaga) {
        this.vaga = vaga;
    }
    
    
}

