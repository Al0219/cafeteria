package com.app.cafeteria.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "metodo_pago")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MetodoPago {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "metodo_pago_seq")
    @SequenceGenerator(name = "metodo_pago_seq", sequenceName = "metodo_pago_id_seq", allocationSize = 1)
    private Integer id;

    @Column(length = 25, nullable = false, unique = true)
    private String nombre;
}
