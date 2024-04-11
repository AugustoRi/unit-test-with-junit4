package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.utils.DataUtils.adicionarDias;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;

public class LocacaoService {

	private LocacaoDAO dao;
	private SerasaService serasaService;
	
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

		if(serasaService.possuiNegativacao(usuario)) {
			throw new LocadoraException("Usuário Negativado");
		}

        Locacao locacao = new Locacao();
		locacao.setFilmes(filmes);
		locacao.setUsuario(usuario);
		locacao.setDataLocacao(new Date());

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
        locacao.setValor(precoLocacaoTotal);

		//Entrega no dia seguinte
		Date dataEntrega = new Date();
		dataEntrega = adicionarDias(dataEntrega, 1);
		if (DataUtils.verificarDiaSemana(dataEntrega, Calendar.SUNDAY)) {
			dataEntrega = adicionarDias(dataEntrega, 1);
		}
		locacao.setDataRetorno(dataEntrega);
		
		//Salvando a locacao...	
		dao.salvar(locacao);
		
		return locacao;
	}

	public void setLocacaoDAO(LocacaoDAO dao) {
		this.dao = dao;
	}

	public void setSerasaService(SerasaService service) {
		serasaService = service;
	}
}