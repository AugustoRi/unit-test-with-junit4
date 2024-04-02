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

import java.util.Date;

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
        System.out.println("Before");
        locacaoService = new LocacaoService();
    }

    @After
    public void tearDown() {
        System.out.println("After");
    }

    @BeforeClass
    public static void setupClass() {
        System.out.println("Before Class");
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("After Class");
    }

    @Test
    public void testLocacao() throws Exception {
        //cenario
        Usuario usuario = new Usuario("Usuario 1");
        Filme filme = new Filme("Filme 1", 2, 5.0);

        System.out.println("Teste!");

        //acao
        Locacao locacao = locacaoService.alugarFilme(usuario, filme);

        //verificacao
        error.checkThat(locacao.getValor(), is(equalTo(5.0)));
        error.checkThat(DataUtils.isMesmaData(locacao.getDataLocacao(), new Date()), is(true));
        error.checkThat(DataUtils.isMesmaData(locacao.getDataRetorno(), DataUtils.obterDataComDiferencaDias(1)), is(true));
    }

    //utilizada quando apenas a excecao importa para o teste
    @Test(expected = FilmeSemEstoqueException.class)
    public void testLocacao_filmeSemEstoque() throws Exception {
        //cenario
        Usuario usuario = new Usuario("Usuario 1");
        Filme filme = new Filme("Filme 1", 0, 5.0);

        //acao
        Locacao locacao = locacaoService.alugarFilme(usuario, filme);
    }

    //utilizada quando quero tratar a excecao e inserir mais passos, como imprimir uma mensagem
    //recomendacao para padronizacao, por se tratar da forma mais completa
    @Test
    public void testLocacao_usuarioVazio() throws FilmeSemEstoqueException {
        //cenario
        Filme filme = new Filme("Filme 1", 1, 4.0);

        //acao
        try {
            Locacao locacao = locacaoService.alugarFilme(null, filme);
            Assert.fail();
        } catch (LocadoraException e) {
            Assert.assertThat(e.getMessage(), is("Usuario vazio"));
        }

        //consigo inserir mais passos e refletir depois de tratar a excecao
        System.out.println("Forma robusta");
    }

    //utilizada quando quero tratar a excecao e inserir mais passos, como imprimir uma mensagem
    //atende a maioria dos casos, mas tem alguns que somente a forma robusta seria o ideal
    @Test
    public void testLocacao_filmeVazio() throws FilmeSemEstoqueException, LocadoraException {
        //cenario
        Usuario usuario = new Usuario("Usuario 1");

        expectedException.expect(LocadoraException.class);
        expectedException.expectMessage("Filme vazio");

        //acao
        Locacao locacao = locacaoService.alugarFilme(usuario, null);

        System.out.println("Forma com expectedException");
    }
}
