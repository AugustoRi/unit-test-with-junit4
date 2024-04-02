package br.ce.wcaquino.servicos;

import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;
import org.junit.*;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class LocacaoServiceTest {
    private LocacaoService locacaoService;

    @Rule
    public ErrorCollector error = new ErrorCollector();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        locacaoService = new LocacaoService();
    }

    @Test
    public void deveAlugarFilmeComSucesso() throws Exception {
        //cenario
        Usuario usuario = new Usuario("Usuario 1");
        List<Filme> filmes = new ArrayList<>();
        filmes.add(new Filme("Filme 1", 2, 5.0));
        filmes.add(new Filme("Filme 2", 5, 5.0));

        //acao
        Locacao locacao = locacaoService.alugarFilme(usuario, filmes);

        //verificacao
        error.checkThat(locacao.getValor(), is(equalTo(10.0)));
        error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
        error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterDataComDiferencaDias(1)), is(true));
    }

    @Test(expected = FilmeSemEstoqueException.class)
    public void naoDeveAlugarFilmeSemEstoque() throws Exception {
        //cenario
        Usuario usuario = new Usuario("Usuario 1");
        List<Filme> filmes = new ArrayList<>();
        filmes.add(new Filme("Filme 1", 0, 5.0));
        filmes.add(new Filme("Filme 2", 6, 5.0));

        //acao
        Locacao locacao = locacaoService.alugarFilme(usuario, filmes);
    }

    @Test
    public void naoDeveAlugarFilmeSemUsuario() throws FilmeSemEstoqueException {
        //cenario
        List<Filme> filmes = new ArrayList<>();
        filmes.add(new Filme("Filme 1", 2, 5.0));
        filmes.add(new Filme("Filme 2", 5, 5.0));

        //acao
        try {
            Locacao locacao = locacaoService.alugarFilme(null, filmes);
            Assert.fail();
        } catch (LocadoraException e) {
            Assert.assertThat(e.getMessage(), is("Usuario vazio"));
        }
    }

    @Test
    public void naoDeveAlugarFilmeSemAlgumFilme() throws FilmeSemEstoqueException, LocadoraException {
        //cenario
        Usuario usuario = new Usuario("Usuario 1");
        List<Filme> filmes = new ArrayList<>();
        filmes.add(new Filme("Filme 1", 2, 5.0));
        filmes.add(null);

        expectedException.expect(LocadoraException.class);
        expectedException.expectMessage("Filme vazio");

        //acao
        Locacao locacao = locacaoService.alugarFilme(usuario, filmes);
    }

    @Test
    public void naoDeveAlugarFilmeSemFilmes() throws FilmeSemEstoqueException, LocadoraException {
        //cenario
        Usuario usuario = new Usuario("Usuario 1");

        expectedException.expect(LocadoraException.class);
        expectedException.expectMessage("Lista de filmes vazia");

        //acao
        Locacao locacao = locacaoService.alugarFilme(usuario, null);

        System.out.println("Forma com expectedException");
    }

    @Test
    public void deveAplicar25PctDeDescontoNoFilme3() throws FilmeSemEstoqueException, LocadoraException {
        //cenario
        Usuario usuario = new Usuario("Usuario 1");
        List<Filme> filmes = new ArrayList<>();
        filmes.add(new Filme("Filme 1", 2, 4.0));
        filmes.add(new Filme("Filme 2", 2, 4.0));
        filmes.add(new Filme("Filme 3", 2, 4.0));

        //acao
        Locacao resultado = locacaoService.alugarFilme(usuario, filmes);

        //verificacao
        assertThat(resultado.getValor(), is(11.0));
    }

    @Test
    public void deveAplicar50PctDeDescontoNoFilme4() throws FilmeSemEstoqueException, LocadoraException {
        //cenario
        Usuario usuario = new Usuario("Usuario 1");
        List<Filme> filmes = new ArrayList<>();
        filmes.add(new Filme("Filme 1", 2, 4.0));
        filmes.add(new Filme("Filme 2", 2, 4.0));
        filmes.add(new Filme("Filme 3", 2, 4.0));
        filmes.add(new Filme("Filme 4", 2, 4.0));

        //acao
        Locacao resultado = locacaoService.alugarFilme(usuario, filmes);

        //verificacao
        assertThat(resultado.getValor(), is(13.0));
    }

    @Test
    public void deveAplicar75PctDeDescontoNoFilme5() throws FilmeSemEstoqueException, LocadoraException {
        //cenario
        Usuario usuario = new Usuario("Usuario 1");
        List<Filme> filmes = new ArrayList<>();
        filmes.add(new Filme("Filme 1", 2, 4.0));
        filmes.add(new Filme("Filme 2", 2, 4.0));
        filmes.add(new Filme("Filme 3", 2, 4.0));
        filmes.add(new Filme("Filme 4", 2, 4.0));
        filmes.add(new Filme("Filme 5", 2, 4.0));

        //acao
        Locacao resultado = locacaoService.alugarFilme(usuario, filmes);

        //verificacao
        assertThat(resultado.getValor(), is(14.0));
    }

    @Test
    public void deveAplicar100PctDeDescontoNoFilme6() throws FilmeSemEstoqueException, LocadoraException {
        //cenario
        Usuario usuario = new Usuario("Usuario 1");
        List<Filme> filmes = new ArrayList<>();
        filmes.add(new Filme("Filme 1", 2, 4.0));
        filmes.add(new Filme("Filme 2", 2, 4.0));
        filmes.add(new Filme("Filme 3", 2, 4.0));
        filmes.add(new Filme("Filme 4", 2, 4.0));
        filmes.add(new Filme("Filme 5", 2, 4.0));
        filmes.add(new Filme("Filme 6", 2, 4.0));

        //acao
        Locacao resultado = locacaoService.alugarFilme(usuario, filmes);

        //verificacao
        assertThat(resultado.getValor(), is(14.0));
    }
}
