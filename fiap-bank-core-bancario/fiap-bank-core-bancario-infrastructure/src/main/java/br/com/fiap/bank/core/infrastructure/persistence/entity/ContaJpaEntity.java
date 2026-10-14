package br.com.fiap.bank.core.infrastructure.persistence.entity;

import br.com.fiap.bank.core.domain.model.StatusConta;
import br.com.fiap.bank.core.domain.model.TipoConta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tb_conta")
@Getter
@Setter
@NoArgsConstructor
public class ContaJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String numero;

    @Column(nullable = false, length = 4)
    private String agencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoConta tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusConta status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo;

    @Column(name = "data_abertura", nullable = false)
    private LocalDate dataAbertura;

    // Um cliente pode ter contas antigas encerradas, por isso nao e unico
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private ClienteJpaEntity cliente;
}
