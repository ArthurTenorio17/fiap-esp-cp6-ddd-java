package br.com.fiap.bank.core.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tb_cliente")
@Getter
@Setter
@NoArgsConstructor
public class ClienteJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false)
    private String email;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "renda_mensal", nullable = false, precision = 15, scale = 2)
    private BigDecimal rendaMensal;
}
