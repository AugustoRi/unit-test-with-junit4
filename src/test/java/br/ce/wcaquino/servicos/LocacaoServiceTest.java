package br.ce.wcaquino.servicos;

import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import org.junit.*;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.*;

import static br.ce.wcaquino.builders.FilmeBuilder.*;
import static br.ce.wcaquino.builders.LocacaoBuilder.*;
import static br.ce.wcaquino.builders.UsuarioBuilder.umUsuario;
import static br.ce.wcaquino.matchers.MatchersProprios.*;
import static br.ce.wcaquino.utils.DataUtils.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class LocacaoServiceTest {
    private LocacaoService locacaoService;
    private SerasaService serasaService;
    private LocacaoDAO locacaoDAO;
    private EmailService emailService;

    @Rule
    public ErrorCollector error = new ErrorCollector();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        locacaoService = new LocacaoService();

        locacaoDAO = mock(LocacaoDAO.class);
        locacaoService.setLocacaoDAO(locacaoDAO);

        serasaService = mock(SerasaService.class);
        locacaoService.setSerasaService(serasaService);

        emailService = mock(EmailService.class);
        locacaoService.setEmailService(emailService);
    }

    @Test
    public void deveAlugarFilmeComSucesso() throws Exception {
        Assume.assumeFalse(verificarDiaSemana(new Date(), Calendar.SATURDAY));

        //cenario
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = Arrays.asList(
                umFilme().comValor(5.0).agora(),
                umFilme().comValor(5.0).agora()
        );

        //acao
        Locacao locacao = locacaoService.alugarFilme(usuario, filmes);

        //verificacao
        error.checkThat(locacao.getValor(), is(equalTo(10.0)));
        error.checkThat(locacao.getDataLocacao(), ehHoje());
        error.checkThat(locacao.getDataRetorno(), ehHojeComDiferencaDias(1));
    }

    @Test(expected = FilmeSemEstoqueException.class)
    public void naoDeveAlugarFilmeSemEstoque() throws Exception {
        //cenario
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = Collections.singletonList(umFilme().semEstoque().agora());

        //acao
        Locacao locacao = locacaoService.alugarFilme(usuario, filmes);
    }

    @Test
    public void naoDeveAlugarFilmeSemUsuario() throws FilmeSemEstoqueException {
        //cenario
        List<Filme> filmes = Collections.singletonList(umFilme().agora());

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
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = Arrays.asList(umFilme().agora(), null);

        expectedException.expect(LocadoraException.class);
        expectedException.expectMessage("Filme vazio");

        //acao
        Locacao locacao = locacaoService.alugarFilme(usuario, filmes);
    }

    @Test
    public void naoDeveAlugarFilmeSemFilmes() throws FilmeSemEstoqueException, LocadoraException {
        //cenario
        Usuario usuario = umUsuario().agora();

        expectedException.expect(LocadoraException.class);
        expectedException.expectMessage("Lista de filmes vazia");

        //acao
        Locacao locacao = locacaoService.alugarFilme(usuario, null);
    }

    @Test
    //@Ignore -> bom para deixar o test em stand-by, por algum motivo, e nao perder ele, por conta que sera mostrado que ele foi ignorado
    public void deveDevolverNaSegundaAoAlugarSabado() throws FilmeSemEstoqueException, LocadoraException {
        Assume.assumeTrue(verificarDiaSemana(new Date(), Calendar.SATURDAY));

        //cenario
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = Collections.singletonList(umFilme().agora());

        //acao
        Locacao retorno = locacaoService.alugarFilme(usuario, filmes);

        //verificacao
        assertThat(retorno.getDataRetorno(), caiNumaSegunda());
    }

    @Test
    public void naoDeveAlugarFilmeParaNegativadoSerasa() throws FilmeSemEstoqueException {
        //cenario
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = Collections.singletonList(umFilme().agora());

        when(serasaService.possuiNegativacao(Mockito.any(Usuario.class))).thenReturn(true);

        //acao
        try {
            locacaoService.alugarFilme(usuario, filmes);
        //verificacao
            fail();
        } catch (LocadoraException e) {
            assertThat(e.getMessage(), is("Usuário Negativado"));
        }

        //verificacao
        verify(serasaService).possuiNegativacao(usuario);
    }

    @Test
    public void deveEnviarEmailParaLocacoesAtrasadas() {
        //cenario
        Usuario usuario = umUsuario().agora();
        Usuario usuario2 = umUsuario().comNome("Usuario em dia").agora();
        Usuario usuario3 = umUsuario().comNome("Outro atrasado").agora();
        List<Locacao> locacoes = Arrays.asList(
                umLocacao().atrasada().comUsuario(usuario).agora(),
                umLocacao().comUsuario(usuario2).agora(),
                umLocacao().atrasada().comUsuario(usuario3).agora(),
                umLocacao().atrasada().comUsuario(usuario3).agora()
        );
        when(locacaoDAO.obterLocacoesPendentes()).thenReturn(locacoes);

        //acao
        locacaoService.notificarAtrasos();

        //verificacao
        verify(emailService, times(3)).notificarAtraso(Mockito.any(Usuario.class));
        verify(emailService).notificarAtraso(usuario);
        verify(emailService, atLeastOnce()).notificarAtraso(usuario3);
        verify(emailService, never()).notificarAtraso(usuario2);
        verifyNoMoreInteractions(emailService);
    }
}
