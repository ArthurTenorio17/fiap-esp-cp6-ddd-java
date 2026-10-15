package br.com.fiap.bank.plataforma.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SolicitacaoCredito {

    private Long id;
    private String cpfCliente;
    private String nomeCliente;
    private String tipoCredito;
    private BigDecimal valorSolicitado;
    private Integer quantidadeParcelas;
    private BigDecimal rendaMensal;
    private Integer scoreCredito;
    private StatusSolicitacao status;
    private String parecerOperador;
    private Boolean scrRestricoes;
    private Integer scoreBureau;
    private LocalDateTime dataSolicitacao;
    private LocalDateTime dataAtualizacao;

    public SolicitacaoCredito() {
    }

    public SolicitacaoCredito(
            Long id,
            String cpfCliente,
            String nomeCliente,
            String tipoCredito,
            BigDecimal valorSolicitado,
            Integer quantidadeParcelas,
            BigDecimal rendaMensal,
            Integer scoreCredito,
            StatusSolicitacao status,
            String parecerOperador,
            Boolean scrRestricoes,
            Integer scoreBureau,
            LocalDateTime dataSolicitacao,
            LocalDateTime dataAtualizacao
    ) {
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

    public void aprovar(String parecer) {
        validarDecisao(parecer);

        this.status = StatusSolicitacao.APROVADO;
        this.parecerOperador = parecer;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void reprovar(String parecer) {
        validarDecisao(parecer);

        this.status = StatusSolicitacao.REPROVADO;
        this.parecerOperador = parecer;
        this.dataAtualizacao = LocalDateTime.now();
    }

    private void validarDecisao(String parecer) {
        if (status == StatusSolicitacao.APROVADO ||
                status == StatusSolicitacao.REPROVADO) {
            throw new IllegalStateException(
                    "A solicitacao ja possui uma decisao final."
            );
        }

        if (parecer == null || parecer.isBlank()) {
            throw new IllegalArgumentException(
                    "O parecer do operador e obrigatorio."
            );
        }

        if (parecer.length() > 500) {
            throw new IllegalArgumentException(
                    "O parecer deve ter no maximo 500 caracteres."
            );
        }
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
