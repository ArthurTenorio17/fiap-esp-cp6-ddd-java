package br.com.fiap.bank.plataforma.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_solicitacao_credito")
public class SolicitacaoCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cpf_cliente", nullable = false, length = 11)
    private String cpfCliente;

    @Column(name = "nome_cliente", nullable = false)
    private String nomeCliente;

    @Column(name = "tipo_credito", nullable = false)
    private String tipoCredito;

    @Column(name = "valor_solicitado", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorSolicitado;

    @Column(name = "quantidade_parcelas", nullable = false)
    private Integer quantidadeParcelas;

    @Column(name = "renda_mensal", nullable = false, precision = 15, scale = 2)
    private BigDecimal rendaMensal;

    @Column(name = "score_credito")
    private Integer scoreCredito;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSolicitacao status;

    @Column(name = "parecer_operador", length = 500)
    private String parecerOperador;

    @Column(name = "scr_restricoes")
    private Boolean scrRestricoes;

    @Column(name = "score_bureau")
    private Integer scoreBureau;

    @Column(name = "data_solicitacao", nullable = false)
    private LocalDateTime dataSolicitacao;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    public SolicitacaoCredito() {}

    public SolicitacaoCredito(Long id, String cpfCliente, String nomeCliente, String tipoCredito, BigDecimal valorSolicitado, Integer quantidadeParcelas, BigDecimal rendaMensal, Integer scoreCredito, StatusSolicitacao status, String parecerOperador, Boolean scrRestricoes, Integer scoreBureau, LocalDateTime dataSolicitacao, LocalDateTime dataAtualizacao) {
        this.id = id;
        this.cpfCliente = cpfCliente;
        this.nomeCliente = nomeCliente;
        this.tipoCredito = tipoCredito;
        this.valorSolicitado = valorSolicitado;
        this.quantidadeParcelas = quantidadeParcelas;
        this.rendaMensal = rendaMensal;
        this.scoreCredito = scoreCredito;
        this.status = status;
        this.parecerOperador = parecerOperador;
        this.scrRestricoes = scrRestricoes;
        this.scoreBureau = scoreBureau;
        this.dataSolicitacao = dataSolicitacao;
        this.dataAtualizacao = dataAtualizacao;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCpfCliente() { return cpfCliente; }
    public void setCpfCliente(String cpfCliente) { this.cpfCliente = cpfCliente; }

    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }

    public String getTipoCredito() { return tipoCredito; }
    public void setTipoCredito(String tipoCredito) { this.tipoCredito = tipoCredito; }

    public BigDecimal getValorSolicitado() { return valorSolicitado; }
    public void setValorSolicitado(BigDecimal valorSolicitado) { this.valorSolicitado = valorSolicitado; }

    public Integer getQuantidadeParcelas() { return quantidadeParcelas; }
    public void setQuantidadeParcelas(Integer quantidadeParcelas) { this.quantidadeParcelas = quantidadeParcelas; }

    public BigDecimal getRendaMensal() { return rendaMensal; }
    public void setRendaMensal(BigDecimal rendaMensal) { this.rendaMensal = rendaMensal; }

    public Integer getScoreCredito() { return scoreCredito; }
    public void setScoreCredito(Integer scoreCredito) { this.scoreCredito = scoreCredito; }

    public StatusSolicitacao getStatus() { return status; }
    public void setStatus(StatusSolicitacao status) { this.status = status; }

    public String getParecerOperador() { return parecerOperador; }
    public void setParecerOperador(String parecerOperador) { this.parecerOperador = parecerOperador; }

    public Boolean getScrRestricoes() { return scrRestricoes; }
    public void setScrRestricoes(Boolean scrRestricoes) { this.scrRestricoes = scrRestricoes; }

    public Integer getScoreBureau() { return scoreBureau; }
    public void setScoreBureau(Integer scoreBureau) { this.scoreBureau = scoreBureau; }

    public LocalDateTime getDataSolicitacao() { return dataSolicitacao; }
    public void setDataSolicitacao(LocalDateTime dataSolicitacao) { this.dataSolicitacao = dataSolicitacao; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
}
