
package br.com.fiap.bank.core.application.service;

import br.com.fiap.bank.core.application.dto.AtualizarSaldoRequest;
import br.com.fiap.bank.core.application.dto.ContaResponse;
import br.com.fiap.bank.core.application.dto.CriarContaRequest;
import br.com.fiap.bank.core.application.exception.ConflitoException;
import br.com.fiap.bank.core.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.bank.core.application.exception.RequisicaoInvalidaException;
import br.com.fiap.bank.core.domain.model.Cliente;
import br.com.fiap.bank.core.domain.model.Conta;
import br.com.fiap.bank.core.domain.repository.ClienteRepository;
import br.com.fiap.bank.core.domain.repository.ContaRepository;
import br.com.fiap.bank.core.domain.vo.Cpf;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ContaService {

    private final ContaRepository contaRepository;
    private final ClienteRepository clienteRepository;

    @Transactional
    public ContaResponse abrir(CriarContaRequest request) {
        return abrirComNumero(request, null);
    }

    /** Usado pela carga inicial, que precisa de numeros de conta conhecidos. Sem numero, gera um novo. */
    @Transactional
    public ContaResponse abrirComNumero(CriarContaRequest request, String numeroDesejado) {
        return TradutorDeErros.executar(() -> {
            Cpf cpf = Cpf.of(request.cpfCliente());

            Cliente cliente = clienteRepository.buscarPorCpf(cpf)
                    .orElseThrow(() -> new RequisicaoInvalidaException(
                            "Cliente nao encontrado para o CPF " + cpf));

            if (cliente.getConta() != null && cliente.getConta().estaAtiva()) {
                throw new ConflitoException(
                        "O cliente " + cpf + " ja possui uma conta ativa");
            }

            String numero = numeroDesejado != null
                    ? numeroDesejado
                    : gerarNumeroLivre();

            if (numeroDesejado != null && contaRepository.existePorNumero(numero)) {
                throw new ConflitoException(
                        "Ja existe conta com o numero " + numero);
            }

            Conta conta = Conta.abrir(
                    numero,
                    request.agencia(),
                    request.tipoConta(),
                    cpf,
                    request.saldoInicial()
            );

            cliente.vincularConta(conta);

            return ContaResponse.de(contaRepository.salvar(conta));
        });
    }

    @Transactional(readOnly = true)
    public List<ContaResponse> listar() {
        return contaRepository.listarTodas()
                .stream()
                .map(ContaResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContaResponse buscarPorNumero(String numeroConta) {
        return ContaResponse.de(obterPorNumero(numeroConta));
    }

    @Transactional(readOnly = true)
    public ContaResponse buscarPorCpfTitular(String cpf) {
        return TradutorDeErros.executar(() -> {
            Cpf documento = Cpf.of(cpf);

            return contaRepository.buscarPorCpfTitular(documento)
                    .map(ContaResponse::de)
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Nenhuma conta encontrada para o CPF " + documento));
        });
    }

    @Transactional
    public ContaResponse atualizarSaldo(String numeroConta, AtualizarSaldoRequest request) {
        Conta conta = obterPorNumero(numeroConta);

        return TradutorDeErros.executar(() -> {
            if ("DEPOSITO".equalsIgnoreCase(request.operacao())) {
                conta.depositar(request.valor());
            } else if ("SAQUE".equalsIgnoreCase(request.operacao())) {
                conta.sacar(request.valor());
            } else {
                throw new RequisicaoInvalidaException(
                        "Operacao invalida: utilize DEPOSITO ou SAQUE"
                );
            }

            return ContaResponse.de(contaRepository.salvar(conta));
        });
    }

    /** Inativacao logica: a conta continua gravada com status ENCERRADA. */
    @Transactional
    public void encerrar(String numeroConta) {
        Conta conta = obterPorNumero(numeroConta);
        conta.encerrar();
        contaRepository.salvar(conta);
    }

    private Conta obterPorNumero(String numeroConta) {
        return contaRepository.buscarPorNumero(numeroConta)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Conta nao encontrada: " + numeroConta));
    }

    private String gerarNumeroLivre() {
        String numero;

        do {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            numero = random.nextInt(10000, 100000)
                    + "-"
                    + random.nextInt(10);
        } while (contaRepository.existePorNumero(numero));

        return numero;
    }
}
