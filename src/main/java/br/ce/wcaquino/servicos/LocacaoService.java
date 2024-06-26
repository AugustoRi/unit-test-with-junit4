package br.ce.wcaquino.servicos;

import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static br.ce.wcaquino.utils.DataUtils.adicionarDias;

public class LocacaoService {

	private LocacaoDAO dao;
	private SerasaService serasaService;
	private EmailService emailService;
	
	public Locacao alugarFilme(Usuario usuario, List<Filme> filmes) throws FilmeSemEstoqueException, LocadoraException {
		if (usuario == null) {
			throw new LocadoraException("Usuario vazio");
		}

		if (filmes == null || filmes.isEmpty()) {
			throw new LocadoraException("Lista de filmes vazia");
		}

        for (Filme filme : filmes) {
            if (filme == null) {
                throw new LocadoraException("Filme vazio");
            }

            if (filme.getEstoque() == 0) {
                throw new FilmeSemEstoqueException();
            }
        }

		boolean negativado;

        try {
			negativado = serasaService.possuiNegativacao(usuario);
        } catch (Exception e) {
            throw new LocadoraException("Problema com Serasa, tente novamente");
        }

		if(negativado) {
			throw new LocadoraException("Usuário Negativado");
		}

        Locacao locacao = new Locacao();
		locacao.setFilmes(filmes);
		locacao.setUsuario(usuario);
		locacao.setDataLocacao(obterData());
		locacao.setValor(calcularValorLocacao(filmes));

		//Entrega no dia seguinte
		Date dataEntrega = obterData();
		dataEntrega = adicionarDias(dataEntrega, 1);
		if (DataUtils.verificarDiaSemana(dataEntrega, Calendar.SUNDAY)) {
			dataEntrega = adicionarDias(dataEntrega, 1);
		}
		locacao.setDataRetorno(dataEntrega);
		
		//Salvando a locacao...	
		dao.salvar(locacao);
		
		return locacao;
	}

	protected Date obterData() {
		return new Date();
	}

	private Double calcularValorLocacao(List<Filme> filmes) {
		Double precoLocacaoTotal = 0d;
		for (int i = 0; i < filmes.size(); i++) {
			Filme filme = filmes.get(i);
			Double precoLocacaoDoFilme = filme.getPrecoLocacao();
			switch (i) {
				case 2: {
					Double desconto = precoLocacaoDoFilme * 0.25;
					precoLocacaoDoFilme = precoLocacaoDoFilme - desconto;
					break;
				}
				case 3: {
					Double desconto = precoLocacaoDoFilme * 0.5;
					precoLocacaoDoFilme = precoLocacaoDoFilme - desconto;
					break;
				}
				case 4: {
					Double desconto = precoLocacaoDoFilme * 0.75;
					precoLocacaoDoFilme = precoLocacaoDoFilme - desconto;
					break;
				}
				case 5: {
					precoLocacaoDoFilme = 0d;
					break;
				}
				default:
					break;
			}
			precoLocacaoTotal += precoLocacaoDoFilme;
		}
		return precoLocacaoTotal;
	}

	public void notificarAtrasos() {
		List<Locacao> locacoes = dao.obterLocacoesPendentes();
		locacoes.forEach(locacao -> {
			if(locacao.getDataRetorno().before(obterData())) {
				emailService.notificarAtraso(locacao.getUsuario());
			}
		});
	}

	public void prorrogarLocacao(Locacao locacao, int dias) {
		Locacao novaLocacao = new Locacao();
		novaLocacao.setUsuario(locacao.getUsuario());
		novaLocacao.setFilmes(locacao.getFilmes());
		novaLocacao.setDataLocacao(obterData());
		novaLocacao.setDataRetorno(DataUtils.obterDataComDiferencaDias(dias));
		novaLocacao.setValor(locacao.getValor() * dias);
		dao.salvar(novaLocacao);
	}
}