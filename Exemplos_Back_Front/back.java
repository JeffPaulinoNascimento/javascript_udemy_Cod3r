package br.com.preventsenior.plantao.controller;

import br.com.caelum.vraptor.*;
import br.com.caelum.vraptor.view.Results;
import br.com.preventsenior.agendamento.client.exception.AgendamentoException;
import br.com.preventsenior.agendamento.client.model.AgendaSala;
import br.com.preventsenior.cockpit.client.service.interfaces.rest.AgendamentoRest;
import br.com.preventsenior.core.client.dao.generico.DAOException;
import br.com.preventsenior.core.client.exception.PreventSeniorException;
import br.com.preventsenior.corporativo.client.interfaces.EspecialidadeRemote;
import br.com.preventsenior.corporativo.client.interfaces.SubEspecialidadeRemote;
import br.com.preventsenior.corporativo.client.model.DivisaoOperacional;
import br.com.preventsenior.corporativo.client.model.Especialidade;
import br.com.preventsenior.corporativo.client.model.SubEspecialidade;
import br.com.preventsenior.credenciamento.dto.ChaveValorDTO;
import br.com.preventsenior.credenciamento.model.Prestador;
import br.com.preventsenior.credenciamento.model.Profissional;
import br.com.preventsenior.credenciamento.model.TipoPrestador;
import br.com.preventsenior.credenciamento.prestador.model.enums.TipoPrestadorEnum;
import br.com.preventsenior.credenciamento.service.interfaces.ProfissionalRemote;
import br.com.preventsenior.indicacao.dto.GrupoMarcacaoDTO;
import br.com.preventsenior.indicacao.model.GrupoMarcacao;
import br.com.preventsenior.indicacao.service.interfaces.GrupoMarcacaoRemote;
import br.com.preventsenior.indicacao.service.interfaces.IndicacaoAtendimentoRemote;
import br.com.preventsenior.plantao.business.AgendamentoPlantaoBusiness;
import br.com.preventsenior.plantao.dao.*;
import br.com.preventsenior.plantao.enums.PlantaoHistoricoEnum;
import br.com.preventsenior.plantao.enums.PlantaoHistoricoOrigemEnum;
import br.com.preventsenior.plantao.enums.TipoPagamentoUnidadeEnum;
import br.com.preventsenior.plantao.externo.business.AcessoAgrupamentoBusiness;
import br.com.preventsenior.plantao.externo.vo.ValidacaoHorasVO;
import br.com.preventsenior.plantao.model.*;
import br.com.preventsenior.plantao.model.enums.*;
import br.com.preventsenior.plantao.utils.CalculadorValorPlantao;
import br.com.preventsenior.plantao.utils.DateUtils;
import br.com.preventsenior.plantao.vo.*;
import br.com.preventsenior.portalweb.client.model.Usuario;
import br.com.preventsenior.portalweb.client.sso.UsuarioLogado;
import br.com.preventsenior.portalweb.client.tx.Transactional;
import br.com.preventsenior.portalweb.client.vo.ChaveValorVO;
import com.google.common.base.Strings;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class AgendamentoPlantaoController {

    @Inject
    private Result result;

    @Inject
    @UsuarioLogado
    private Usuario usuario;

    @Inject
    private AcessoUnidadeDAO acessoUnidadeDAO;

    @Inject
    private LocalAtendimentoPlantaoDAO localAtendimentoPlantaoDAO;

    @Inject
    private PlantaoPlanejadoDAO plantaoPlanejadoDAO;

    @Inject
    private PlantaoRealizadoDAO plantaoRealizadoDAO;

    @Inject
    private PlantaoAprovadoDAO plantaoAprovadoDAO;

    @Inject
    private ProfissionalVigenciaDAO profissionalVigenciaDAO;

    @Inject
    private PlantaoEspecialidadeDAO plantaoEspecialidadeDAO;

    @Inject
    private SubEspecialidadeRemote subEspecialidadeRemote;

    @Inject
    private EspecialidadeRemote especialidadeRemote;

    @Inject
    private PausaPlantaoPlanejadoDAO pausaPlantaoPlanejadoDAO;

    @Inject
    private PausaPlantaoAprovacaoDAO pausaPlantaoAprovacaoDAO;

    @Inject
    private PausaPlantaoRealizadoDAO pausaPlantaoRealizadoDAO;

    @Inject
    private PlantaoPlanejadoHoraExtraDAO plantaoPlanejadoHoraExtraDAO;

    @Inject
    private IndicacaoAtendimentoRemote indicacaoAtendimentoRemote;

    @Inject
    private CalculadorValorPlantao calculadorValorPlantao;

    @Inject
    private ProfissionalRemote profissionalRemote;

    @Inject
    private Event<IndexerIndicacaoEvent> eventoProcessadorIndexerIndicacao;

    @Inject
    private Event<ProcessadorHistoricoEvent> processadorHistorico;

    @Inject
    private AgendamentoRest agendamentoRest;

    @Inject
    private CategoriaDAO categoriaDAO;

    @Inject
    private AgendamentoPlantaoBusiness agendamentoPlantaoBusiness;

    @Inject
    private ControleExtratoPagamentoDetalhadoDAO controleExtratoPagamentoDetalhadoDAO;

    @Inject
    private PlantaoPlanejadoGrupoMarcacaoDAO plantaoPlanejadoGrupoMarcacaoDAO;

    @Inject
    private GrupoMarcacaoRemote grupoMarcacaoRemote;

    @Inject
    private AcessoAgrupamentoBusiness acessoAgrupamentoBusiness;

    @Inject
    private ControleExtratoPagamentoFechamentoDAO controleExtratoPagamentoFechamentoDAO;

    @Inject
    private DateUtils dateUtils;

    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
    private SimpleDateFormat sdfMes = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat sdfCompleto = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private SimpleDateFormat sdfSeparado = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private SimpleDateFormat sdfHour = new SimpleDateFormat("HH:mm");

    @Transactional
    @Path("/agendamento/listaUnidades")
    public void listaUnidades() throws DAOException, ParseException {

        List<AcessoUnidadeVO> unidades = acessoAgrupamentoBusiness.preparAgrupamento(usuario.getId());

        if (unidades.size() == 1 && unidades.get(0).getDivisoes().size() == 1) {
            result.forwardTo(this).agendamentoPlantao(unidades.get(0).getLocal().getId());
        } else {
            result.include("unidades", unidades);
        }
    }

    @Transactional
    @Path("/agendamento/verCalendario/{idLocal}")
    public void agendamentoPlantao(Long idLocal) throws DAOException {

        if (acessoUnidadeDAO.temAcessoNessaUnidade(idLocal, usuario.getId())) {
            LocalAtendimentoPlantao local = localAtendimentoPlantaoDAO.find(idLocal);

            List<Especialidade> especialidades = especialidadeRemote.listar();

            result.include("especialidades", especialidades);
            result.include("idLocal", idLocal);
            result.include("local", local);
        } else {
            result.notFound();
        }
    }

    @Post("/agendamento/calendario/listarAgendamentos")
    public void listarAgendamentos(Long start, Long end, Long idLocal, List<Long> profissionais, List<Long> subespecialidades, List<Long> criadores) throws DAOException {
        Date dataInicio = new Date(start);
        Date dataFim = new Date(end);

        List<EventoCalendarioVO> agendamentos = plantaoPlanejadoDAO.buscarNativePorDataLocalTipo(dataInicio, dataFim, idLocal, profissionais, subespecialidades, criadores);

        result.use(Results.json()).withoutRoot().from(agendamentos).serialize();
    }

    @Consumes("application/json")
    @Transactional
    @Post("/agendamento/calendario/salvarEvento")
    public void salvarEvento(List<PausaPlantaoPlanejado> pausas, boolean edicaoCustomizada, EventoCalendarioVO agendamento, String[] horariosExtras) throws Exception {

        /*** ESSE TRECHO FOI ADICIONADO PARA ARRUMAR O ENVIO ERRADO DE GMT PELO MOMENT NO FORMULARIO ***/
        Date dtInicio = sdfCompleto.parse(agendamento.getStart());
        Date dtFim = sdfCompleto.parse(agendamento.getEnd());

        Strings.isNullOrEmpty("d");

        agendamento.setStartLong(dtInicio.getTime());
        agendamento.setEndLong(dtFim.getTime());

        List<Long> ids = new ArrayList<>();
        PlantaoHistoricoEnum statusPlantao;
        List<ChaveValorVO> avisos = new ArrayList<>();

        for (PausaPlantaoPlanejado pausa : pausas) {
            pausa.setDataInicio(sdfCompleto.parse(pausa.getStDataInicio()));
            pausa.setDataFim(sdfCompleto.parse(pausa.getStDataFim()));
        }

        if (edicaoCustomizada) {
            PlantaoPlanejado plantao = plantaoPlanejadoDAO.find(agendamento.getId());
            statusPlantao = PlantaoHistoricoEnum.PLANTAO_ALTERADO;
            ids.add(plantao.getId());

            if (agendamento.getEndDate() != null) {
                plantao.setDataFim(agendamento.getEndDate());
            }

            ArrayList<PausaPlantaoPlanejado> pausasAux = new ArrayList<PausaPlantaoPlanejado>();
            if (!pausas.isEmpty()) {
                for (PausaPlantaoPlanejado pausa : pausas) {
                    pausasAux.add(pausaPlantaoPlanejadoDAO.find(pausa.getId()));
                }
            } else if (plantao.getPausas() != null && !plantao.getPausas().isEmpty()) {
                for (PausaPlantaoPlanejado pausa : plantao.getPausas()) {
                    pausaPlantaoPlanejadoDAO.delete(pausa);
                }
            }

            if (agendamento.getDuracaoConsulta() != null) {
                plantao.setDuracaoConsulta(DuracaoConsultaEnum.get(agendamento.getDuracaoConsulta()));
            } else {
                plantao.setDuracaoConsulta(null);
            }

            if (agendamento.getIdTipoMarcacao() != null) {
                plantao.setTipoMarcacao(TipoMarcacaoEnum.get(agendamento.getIdTipoMarcacao()));
            } else {
                plantao.setTipoMarcacao(null);
            }

            if (agendamento.getAtivo()) {
                plantao.setStatus(StatusPlantaoEnum.ATIVO);
            } else {
                plantao.setStatus(StatusPlantaoEnum.OCULTO);
            }

            //atualiza as categorias quando editar o plantão
            List<Categoria> categorias = new ArrayList<Categoria>();
            for (Long idCategoria : agendamento.getCategorias()) {
                categorias.add(new Categoria(idCategoria));
            }

            //atualiza os grupos quando editar o plantão
            plantaoPlanejadoGrupoMarcacaoDAO.deletarGruposDoPlantao(agendamento.getId());
            List<PlantaoPlanejadoGrupoMarcacao> grupos = new ArrayList<>();
            for (Long idGrupo : agendamento.getGrupos()) {
                GrupoMarcacao grupoMarcacao = new GrupoMarcacao();
                grupoMarcacao.setId(idGrupo);
                grupos.add(new PlantaoPlanejadoGrupoMarcacao(plantao, grupoMarcacao));
            }

            plantao.setGrupos(grupos);

            plantao.setCategorias(categorias);

            plantao.setPausas(pausasAux);

            plantao = plantaoPlanejadoDAO.update(plantao);

        } else {

            agendamento.setId(agendamento.getId() == 0l ? null : agendamento.getId());

            if (agendamento.getId() == null) {
                PlantaoPlanejado plantao = converterParaPlantao(agendamento, pausas, horariosExtras);
                plantao = plantaoPlanejadoDAO.insert(plantao);
                statusPlantao = PlantaoHistoricoEnum.PLANTAO_CRIADO;

                ids.add(plantao.getId());

                boolean pgtoAutomatico = !plantao.getLocalAtendimento().isApontamentoManual();

                if (pgtoAutomatico) {

                    PlantaoRealizado realizado = plantao.getPlantaoRealizado();
                    realizado.setId(plantao.getId());
                    plantaoRealizadoDAO.insert(realizado);
                    PlantaoAprovado aprovado = realizado.getPlantaoAprovado();
                    aprovado.setId(plantao.getId());
                    plantaoAprovadoDAO.insert(aprovado);
                }

                DateTime inicioDoEvento = new DateTime(agendamento.getStartDate());
                DateTime fimDoEvento = new DateTime(agendamento.getEndDate());
                Integer diasAdicionais = 0;

                if (!Strings.isNullOrEmpty(agendamento.getTipoRepeticao())) {

                    switch (agendamento.getTipoRepeticao()) {
                        case "diaria":
                            for (int i = 0; i < agendamento.getQuantidadeRepeticao(); i++) {

                                List<PausaPlantaoPlanejado> pausasRepeticao = new ArrayList<>();

                                ++diasAdicionais;

                                for (PausaPlantaoPlanejado pausaPlanejado : pausas) {

                                    DateTime inicioPausaRepeticao = new DateTime(pausaPlanejado.getDataInicio());
                                    DateTime fimPausaRepeticao = new DateTime(pausaPlanejado.getDataFim());

                                    inicioPausaRepeticao = inicioPausaRepeticao.plusDays(diasAdicionais);
                                    fimPausaRepeticao = fimPausaRepeticao.plusDays(diasAdicionais);

                                    PausaPlantaoPlanejado pausaRepeticao = new PausaPlantaoPlanejado();
                                    pausaRepeticao.setDataInicio(inicioPausaRepeticao.toDate());
                                    pausaRepeticao.setDataFim(fimPausaRepeticao.toDate());

                                    pausasRepeticao.add(pausaRepeticao);
                                }

                                AtendimentoPeriodoVO checagemPlantao = checarAgendamentoNoPeriodo(inicioDoEvento.plusDays(diasAdicionais).toDate().getTime(), fimDoEvento.plusDays(diasAdicionais).toDate().getTime(), agendamento.getIdProfissional(), null);

                                List<TipoPlantaoLocalAtendimento> tiposPlantao = profissionalVigenciaDAO.buscarTiposDePlantaoDoPeriodo(agendamento.getIdLocalAtendimento(), agendamento.getIdProfissional(), inicioDoEvento.plusDays(diasAdicionais).toDate());

                                boolean existeVigencia = false;

                                for (TipoPlantaoLocalAtendimento tipoPlantao : tiposPlantao) {
                                    if (tipoPlantao.getId().equals(agendamento.getIdTipoPlantao())) {
                                        existeVigencia = true;
                                        break;
                                    }
                                }

                                if (!checagemPlantao.isTemAgendamento() && existeVigencia) {
                                    PlantaoPlanejado clone = converterParaPlantao(agendamento, pausasRepeticao, horariosExtras);

                                    clone.setDataInicio(inicioDoEvento.plusDays(diasAdicionais).toDate());
                                    clone.setDataFim(fimDoEvento.plusDays(diasAdicionais).toDate());

                                    clone = atualizaHorariosExtras(clone);

                                    clone = plantaoPlanejadoDAO.insert(clone);

                                    ids.add(clone.getId());

                                    if (pgtoAutomatico) {

                                        PlantaoRealizado cloneRealizado = clone.getPlantaoRealizado();

                                        cloneRealizado.setId(clone.getId());
                                        cloneRealizado.setDataInicio(clone.getDataInicio());
                                        cloneRealizado.setDataFim(clone.getDataFim());

                                        plantaoRealizadoDAO.insert(cloneRealizado);
                                        PlantaoAprovado cloneAprovado = cloneRealizado.getPlantaoAprovado();

                                        cloneAprovado.setId(clone.getId());
                                        cloneAprovado.setDataInicio(clone.getDataInicio());
                                        cloneAprovado.setDataFim(clone.getDataFim());

                                        plantaoAprovadoDAO.insert(cloneAprovado);
                                    }

                                }

                                if (checagemPlantao.isTemAgendamento()) {
                                    avisos.add(new ChaveValorVO("MENSAGEM", "Plantão do dia " + sdf.format(inicioDoEvento.plusDays(diasAdicionais).toDate()) + " não foi criado (Horário já utilizado em " + checagemPlantao.getNomeLocal() + ")"));
                                }

                                if (!existeVigencia) {
                                    avisos.add(new ChaveValorVO("MENSAGEM", "Plantão do dia " + sdf.format(inicioDoEvento.plusDays(diasAdicionais).toDate()) + " não foi criado (Tipo de plantão não tem vigência para este período)"));
                                }
                            }
                            break;
                        case "diasSemana":
                            for (int i = 0; i < agendamento.getQuantidadeRepeticao(); i++) {

                                List<PausaPlantaoPlanejado> pausasRepeticao = new ArrayList<>();

                                ++diasAdicionais;
                                DateTime dia = inicioDoEvento.plusDays(diasAdicionais);

                                while (((dia.getDayOfWeek() == DateTimeConstants.SATURDAY) || (dia.getDayOfWeek() == DateTimeConstants.SUNDAY))) {
                                    dia = inicioDoEvento.plusDays(++diasAdicionais);
                                }

                                for (PausaPlantaoPlanejado pausaPlanejado : pausas) {

                                    DateTime inicioPausaRepeticao = new DateTime(pausaPlanejado.getDataInicio());
                                    DateTime fimPausaRepeticao = new DateTime(pausaPlanejado.getDataFim());

                                    inicioPausaRepeticao = inicioPausaRepeticao.plusDays(diasAdicionais);
                                    fimPausaRepeticao = fimPausaRepeticao.plusDays(diasAdicionais);

                                    PausaPlantaoPlanejado pausaRepeticao = new PausaPlantaoPlanejado();
                                    pausaRepeticao.setDataInicio(inicioPausaRepeticao.toDate());
                                    pausaRepeticao.setDataFim(fimPausaRepeticao.toDate());

                                    pausasRepeticao.add(pausaRepeticao);
                                }

                                AtendimentoPeriodoVO checagemPlantao = checarAgendamentoNoPeriodo(dia.toDate().getTime(), fimDoEvento.plusDays(diasAdicionais).toDate().getTime(), agendamento.getIdProfissional(), null);

                                List<TipoPlantaoLocalAtendimento> tiposPlantao = profissionalVigenciaDAO.buscarTiposDePlantaoDoPeriodo(agendamento.getIdLocalAtendimento(), agendamento.getIdProfissional(), dia.toDate());

                                boolean existeVigencia = false;

                                for (TipoPlantaoLocalAtendimento tipoPlantao : tiposPlantao) {
                                    if (tipoPlantao.getId().equals(agendamento.getIdTipoPlantao())) {
                                        existeVigencia = true;
                                        break;
                                    }
                                }


                                if (!checagemPlantao.isTemAgendamento() && existeVigencia) {
                                    PlantaoPlanejado clone = converterParaPlantao(agendamento, pausasRepeticao, horariosExtras);

                                    clone.setDataInicio(dia.toDate());
                                    clone.setDataFim(fimDoEvento.plusDays(diasAdicionais).toDate());
                                    clone = atualizaHorariosExtras(clone);
                                    clone = plantaoPlanejadoDAO.insert(clone);

                                    ids.add(clone.getId());

                                    if (pgtoAutomatico) {

                                        PlantaoRealizado cloneRealizado = clone.getPlantaoRealizado();

                                        cloneRealizado.setId(clone.getId());
                                        cloneRealizado.setDataInicio(clone.getDataInicio());
                                        cloneRealizado.setDataFim(clone.getDataFim());

                                        plantaoRealizadoDAO.insert(cloneRealizado);
                                        PlantaoAprovado cloneAprovado = cloneRealizado.getPlantaoAprovado();

                                        cloneAprovado.setId(clone.getId());
                                        cloneAprovado.setDataInicio(clone.getDataInicio());
                                        cloneAprovado.setDataFim(clone.getDataFim());

                                        plantaoAprovadoDAO.insert(cloneAprovado);
                                    }

                                }

                                if (checagemPlantao.isTemAgendamento()) {
                                    avisos.add(new ChaveValorVO("MENSAGEM", "Plantão do dia " + sdf.format(dia.toDate().getTime()) + " não foi criado (Horário já utilizado em " + checagemPlantao.getNomeLocal() + ")"));
                                }

                                if (!existeVigencia) {
                                    avisos.add(new ChaveValorVO("MENSAGEM", "Plantão do dia " + sdf.format(dia.toDate().getTime()) + " não foi criado (Tipo de plantão não tem vigência para este período)"));
                                }

                            }
                            break;
                        case "semanal":
                            for (int j = 0; j < agendamento.getDiasRepeticao().size(); j++) {
                                Integer qtdeRepeticoesDoDia = 0;
                                Integer repeticoes = 0;
                                while (qtdeRepeticoesDoDia < agendamento.getQuantidadeRepeticao()) {
                                    DateTime dia = inicioDoEvento.plusDays(++repeticoes);
                                    if (dia.getDayOfWeek() == agendamento.getDiasRepeticao().get(j)) {

                                        List<PausaPlantaoPlanejado> pausasRepeticao = new ArrayList<>();

                                        for (PausaPlantaoPlanejado pausaPlanejado : pausas) {

                                            DateTime inicioPausaRepeticao = new DateTime(pausaPlanejado.getDataInicio());
                                            DateTime fimPausaRepeticao = new DateTime(pausaPlanejado.getDataFim());

                                            inicioPausaRepeticao = inicioPausaRepeticao.plusDays(repeticoes);
                                            fimPausaRepeticao = fimPausaRepeticao.plusDays(repeticoes);

                                            PausaPlantaoPlanejado pausaRepeticao = new PausaPlantaoPlanejado();
                                            pausaRepeticao.setDataInicio(inicioPausaRepeticao.toDate());
                                            pausaRepeticao.setDataFim(fimPausaRepeticao.toDate());

                                            pausasRepeticao.add(pausaRepeticao);
                                        }

                                        AtendimentoPeriodoVO checagemPlantao = checarAgendamentoNoPeriodo(dia.toDate().getTime(), fimDoEvento.plusDays(repeticoes).toDate().getTime(), agendamento.getIdProfissional(), null);

                                        List<TipoPlantaoLocalAtendimento> tiposPlantao = profissionalVigenciaDAO.buscarTiposDePlantaoDoPeriodo(agendamento.getIdLocalAtendimento(), agendamento.getIdProfissional(), dia.toDate());

                                        boolean existeVigencia = false;

                                        for (TipoPlantaoLocalAtendimento tipoPlantao : tiposPlantao) {
                                            if (tipoPlantao.getId().equals(agendamento.getIdTipoPlantao())) {
                                                existeVigencia = true;
                                            }
                                        }

                                        if (!checagemPlantao.isTemAgendamento() && existeVigencia) {
                                            PlantaoPlanejado clone = converterParaPlantao(agendamento, pausasRepeticao, horariosExtras);

                                            clone.setDataInicio(dia.toDate());
                                            clone.setDataFim(fimDoEvento.plusDays(repeticoes).toDate());

                                            clone = atualizaHorariosExtras(clone);
                                            clone = plantaoPlanejadoDAO.insert(clone);

                                            ids.add(clone.getId());

                                            if (pgtoAutomatico) {

                                                PlantaoRealizado cloneRealizado = clone.getPlantaoRealizado();

                                                cloneRealizado.setId(clone.getId());
                                                cloneRealizado.setDataInicio(clone.getDataInicio());
                                                cloneRealizado.setDataFim(clone.getDataFim());

                                                plantaoRealizadoDAO.insert(cloneRealizado);
                                                PlantaoAprovado cloneAprovado = cloneRealizado.getPlantaoAprovado();

                                                cloneAprovado.setId(clone.getId());
                                                cloneAprovado.setDataInicio(clone.getDataInicio());
                                                cloneAprovado.setDataFim(clone.getDataFim());

                                                plantaoAprovadoDAO.insert(cloneAprovado);
                                            }

                                        }

                                        if (checagemPlantao.isTemAgendamento()) {
                                            avisos.add(new ChaveValorVO("MENSAGEM", "Plantão do dia " + sdf.format(dia.toDate()) + " não foi criado (Horário já utilizado em " + checagemPlantao.getNomeLocal() + ")"));
                                        }

                                        if (!existeVigencia) {
                                            avisos.add(new ChaveValorVO("MENSAGEM", "Plantão do dia " + sdf.format(dia.toDate()) + " não foi criado (Tipo de plantão não tem vigência para este período)"));
                                        }

                                        ++qtdeRepeticoesDoDia;
                                    }
                                }
                            }
                            break;
                        default:
                            break;
                    }
                }

            } else {

                ids.add(agendamento.getId());

                plantaoPlanejadoHoraExtraDAO.deletarTodasHorasExtrasPlantaoPlanejado(agendamento.getId());
                plantaoEspecialidadeDAO.deletarEspecialidesDoPlantao(agendamento.getId());
                pausaPlantaoPlanejadoDAO.deletartodasPausasPorPlantaoPlanejado(agendamento.getId());
                plantaoPlanejadoHoraExtraDAO.deletarTodasHorasExtrasPlantaoPlanejado(agendamento.getId());
                plantaoPlanejadoGrupoMarcacaoDAO.deletarGruposDoPlantao(agendamento.getId());

                PlantaoPlanejado plantao = converterParaPlantao(agendamento, pausas, horariosExtras);

                if (!plantao.getLocalAtendimento().isApontamentoManual()) {
                    pausaPlantaoAprovacaoDAO.deletartodasPausasPorPlantaoAprovado(plantao.getId());
                    pausaPlantaoRealizadoDAO.deletartodasPausasPorPlantaoRealizado(plantao.getId());

                    PlantaoRealizado realizado = plantao.getPlantaoRealizado();
                    plantaoRealizadoDAO.update(realizado);
                    PlantaoAprovado aprovado = realizado.getPlantaoAprovado();
                    plantaoAprovadoDAO.update(aprovado);

                }

                plantao = plantaoPlanejadoDAO.update(plantao);
                statusPlantao = PlantaoHistoricoEnum.PLANTAO_ALTERADO;

            }

        }
        eventoProcessadorIndexerIndicacao.fire(new IndexerIndicacaoEvent(ids));
        processadorHistorico.fire(new ProcessadorHistoricoEvent(ids, statusPlantao, PlantaoHistoricoOrigemEnum.PLANTAO_ORIGEM_PLANEJAMENTO));
        result.use(Results.json()).withoutRoot().from(avisos).serialize();

    }

    @Transactional
    @Path("/agendamento/calendario/criarAgendamento")
    public void criarAgendamento(Long inicio, Long fim, Long idLocal) throws DAOException, AgendamentoException {
        if (acessoUnidadeDAO.temAcessoNessaUnidade(idLocal, usuario.getId())) {

            List<GrupoMarcacaoDTO> grupos = grupoMarcacaoRemote.listaGrupoMarcacaoAtivo();

            DateTime agora = new DateTime();
            List<AgendaSala> salas = new ArrayList<>();
            TipoPrestador tipoPrestador = null;

            LocalAtendimentoPlantao local = localAtendimentoPlantaoDAO.find(idLocal);
            Prestador prestadorResponsavel = local.getPrestadorResponsavel();
            List<Especialidade> especialidades = especialidadeRemote.listar();
            List<Categoria> categorias = categoriaDAO.list();

            PlantaoPlanejado agendamento = new PlantaoPlanejado();
            agendamento.setDataInicio(inicio == null ? null : new Date(inicio));
            agendamento.setDataFim(fim == null ? null : new Date(fim));
            agendamento.setEditavel(true);

            boolean edicaoMarcacao = true;
            boolean edicaoSala = true;


            if (prestadorResponsavel != null && (prestadorResponsavel.getTipoPrestadorEnum().equals(TipoPrestadorEnum.DIAGNOSTICO))) {
                salas = agendamentoRest.buscarSalasPorCnpj(local.getPrestadorResponsavel().getCnpj());
                edicaoSala = true;
            } else {
                edicaoSala = false;
            }


            result.include("edicaoMarcacao", edicaoMarcacao);
            result.include("edicaoSala", edicaoSala);
            result.include("especialidades", especialidades);
            result.include("categorias", categorias);
            result.include("local", local);
            result.include("diaDaSemana", agora.getDayOfWeek());
            result.include("evento", agendamento);
            result.include("salas", salas);
            result.include("tipoPrestador", tipoPrestador);
            result.include("tiposAtendimento", TipoAtendimentoEnum.values());
            result.include("duracoes", DuracaoConsultaEnum.values());
            result.include("tiposMarcacao", TipoMarcacaoEnum.values());
            result.include("grupos", grupos);
        } else {
            result.notFound();
        }
    }

    @Transactional
    @Path("/agendamento/calendario/editarAgendamento/{idEvento}")
    public void editarAgendamento(Long idEvento) throws DAOException, PreventSeniorException {

        PlantaoPlanejado agendamento = plantaoPlanejadoDAO.find(idEvento);

        if (agendamento != null) {

            List<GrupoMarcacaoDTO> grupos = grupoMarcacaoRemote.listaGrupoMarcacaoAtivo();

            LocalAtendimentoPlantao local = agendamento.getLocalAtendimento();
            Prestador prestadorResponsavel = local.getPrestadorResponsavel();
            List<TipoPlantaoLocalAtendimento> tipos = profissionalVigenciaDAO.buscarTiposDePlantaoDoPeriodo(local.getId(), agendamento.getProfissional().getId(), agendamento.getDataInicio());
            List<Especialidade> especialidades = especialidadeRemote.listar();
            Especialidade especialidadeBase = agendamento.getEspecialidades().get(0).getSubespecialidade().getEspecialidade();
            List<SubEspecialidade> subEspecialidades = subEspecialidadeRemote.buscarSubEspecialidadesDaEspecialidade(especialidadeBase.getId());
            boolean temIndicacoes = indicacaoAtendimentoRemote.temIndicacoesAtivas(idEvento);
            boolean pgtoAutomatico = !local.isApontamentoManual();
            List<AgendaSala> salas = new ArrayList<>();
            DateTime agora = new DateTime();
            DateTime inicioEvento = new DateTime(agendamento.getDataInicio());
            List<Categoria> categorias = categoriaDAO.list();
            StringBuilder horariosExtrasPlantao = new StringBuilder();

            if (agora.isAfter(inicioEvento)) {
                agendamento.setEditavel(false);
            }

            boolean edicaoMarcacao = true;
            boolean edicaoSala = true;
            boolean edicaoConsulta = true;
            boolean editarFimPlantao = true;
            boolean edicaoCustomizada = false;
            boolean edicaoCategoria = false;

            if (agendamento.getTipoMarcacao() == null || (agendamento.getTipoMarcacao() != null && !agendamento.getTipoMarcacao().equals(TipoMarcacaoEnum.AGENDAMENTO))) {
                edicaoConsulta = false;
            } else if ((agendamento.getTipoMarcacao() != null && agendamento.getTipoMarcacao().equals(TipoMarcacaoEnum.AGENDAMENTO)) && temIndicacoes) {
                edicaoConsulta = false;
            }

            // Verificando se o plantão já está em andamento pelo horário, ou se ainda não começou mas já foi apontado
            if (agendamento.isEmAndamento() || ((agendamento.getPlantaoRealizado() != null && agora.isBefore(agendamento.getDataFim().getTime()) && agendamento.getLocalAtendimento().isApontamentoManual()))) {
                edicaoCustomizada = true;
                editarFimPlantao = true;
                edicaoCategoria = true;
            }

            if (prestadorResponsavel != null && (prestadorResponsavel.getTipoPrestadorEnum().equals(TipoPrestadorEnum.DIAGNOSTICO))) {
                salas = agendamentoRest.buscarSalasPorCnpj(local.getPrestadorResponsavel().getCnpj());
                edicaoSala = true;
            } else {
                edicaoSala = false;
            }

            if (temIndicacoes && agendamento.getTipoMarcacao() != null) {
                edicaoMarcacao = false;

            } else if (agendamento.isEditavel()) {
                edicaoMarcacao = true;
            }

            if (!pgtoAutomatico && agendamento.getPlantaoRealizado() != null && !agendamento.getPlantaoRealizado().getStatus().equals(SituacaoPlantaoEnum.AGUARDANDO_ENTRADA)) {
                agendamento.setEditavel(false);
            }

            if (agendamento.isAposPeriodoDoPlantao()) {
                editarFimPlantao = false;
                edicaoMarcacao = false;
                edicaoSala = false;
                edicaoConsulta = false;
                edicaoCategoria = false;
            }

            if (agendamento.getPlantaoRealizado() != null && (agendamento.getPlantaoRealizado().getStatus().equals(SituacaoPlantaoEnum.AUSENTE) || agendamento.getPlantaoRealizado().getDataFimApontamento() != null)){
                edicaoCategoria = false;
            }

            if (!agendamento.getHorasExtras().isEmpty()) {
                String aux = "";
                DateFormat format = new SimpleDateFormat("HH:mm");
                for (PlantaoPlanejadoHoraExtra horarioExtra : agendamento.getHorasExtras()) {
                    horariosExtrasPlantao.append(aux);
                    aux = ",";
                    horariosExtrasPlantao.append(format.format(horarioExtra.getId().getHorarioExtra()));
                }
            }

            result.include("edicaoSala", edicaoSala);
            result.include("edicaoConsulta", edicaoConsulta);
            result.include("edicaoMarcacao", edicaoMarcacao);
            result.include("edicaoCategoria", edicaoCategoria);
            result.include("especialidadeBase", especialidadeBase);
            result.include("especialidades", especialidades);
            result.include("subEspecialidades", subEspecialidades);
            result.include("categorias", categorias);
            result.include("local", local);
            result.include("tipos", tipos);
            result.include("evento", agendamento);
            result.include("salas", salas);
            result.include("tiposAtendimento", TipoAtendimentoEnum.values());
            result.include("editarFimPlantao", editarFimPlantao);
            result.include("edicaoCustomizada", edicaoCustomizada);
            result.include("duracoes", DuracaoConsultaEnum.values());
            result.include("tiposMarcacao", TipoMarcacaoEnum.values());
            result.include("now", new Date());
            result.include("horariosExtrasPlantao", horariosExtrasPlantao.toString());
            result.include("grupos", grupos);
            result.forwardTo("/WEB-INF/jsp/agendamentoPlantao/criarAgendamento.jsp");
        }
        else{
            result.forwardTo("/WEB-INF/jsp/compartilhados/plantaoNaoEncontrado.jsp");
        }


    }

    @Path("/agendamento/calendario/modalExclusaoEvento/{id}")
    public void modalExclusaoEvento(Long id, String observacao){
        result.include("idPlantao", id);
        result.include("observacao", observacao);
    }

    @Transactional
    @Post("/agendamento/calendario/excluirEvento")
    public void excluirEvento(Long id, String observacao, String motivoExclusao) throws Exception {
        pausaPlantaoAprovacaoDAO.deletartodasPausasPorPlantaoAprovado(id);
        pausaPlantaoRealizadoDAO.deletartodasPausasPorPlantaoRealizado(id);
        pausaPlantaoPlanejadoDAO.deletartodasPausasPorPlantaoPlanejado(id);

        PlantaoPlanejado plantao = plantaoPlanejadoDAO.find(id);

        if (plantao.getPlantaoRealizado() != null) {
            PlantaoRealizado realizado = plantao.getPlantaoRealizado();
            if (realizado.getPlantaoAprovado() != null) {
                PlantaoAprovado aprovado = realizado.getPlantaoAprovado();
                plantaoAprovadoDAO.delete(aprovado);
            }
            plantaoRealizadoDAO.delete(realizado);
        }

        plantaoPlanejadoDAO.delete(plantao);

        List<Long> ids = new ArrayList<>();
        ids.add(id);
        processadorHistorico.fire(new ProcessadorHistoricoEvent(ids, observacao, motivoExclusao));
        result.use(Results.json()).withoutRoot().from("").serialize();
    }

    @Transactional
    @Post("/agendamento/calendario/alterarPeriodoAgendamento")
    public void alterarPeriodoAgendamento(Long id, String start, String end) throws DAOException, ParseException {

        boolean possuiIndicadao = indicacaoAtendimentoRemote.temIndicacoesAtivas(id);

        List<ChaveValorVO> retorno = new ArrayList<>();

        if (!possuiIndicadao) {
            Usuario usuarioExecutante = new Usuario();
            usuarioExecutante.setId(usuario.getId());

            PlantaoPlanejado plantao = plantaoPlanejadoDAO.find(id);

            /*** ESSE TRECHO FOI ADICIONADO PARA ARRUMAR O ENVIO ERRADO DE GMT PELO MOMENT NO FORMULARIO ***/
            Date dtInicio = sdfCompleto.parse(start);
            Date dtFim = sdfCompleto.parse(end);

            plantao.setDataInicio(dtInicio);
            plantao.setDataFim(dtFim);

            plantao = atualizaHorariosExtras(plantao);

            LocalAtendimentoPlantao local = plantao.getTipoPlantao().getLocalAtendimento();

            boolean pgtoAutomatico = !local.isApontamentoManual();

            if (pgtoAutomatico) {
                PlantaoAprovado aprovado = plantaoAprovadoDAO.find(plantao.getId());

                if (aprovado != null) {

                    aprovado.setDataInicio(plantao.getDataInicio());
                    aprovado.setDataFim(plantao.getDataFim());
                    aprovado.setDataAprovacao(new Date());
                    aprovado.setDataLiberacaoPagamento(new Date());
                    aprovado.setUsuario(usuarioExecutante);

                    plantaoAprovadoDAO.update(aprovado);
                }

                PlantaoRealizado realizado = plantaoRealizadoDAO.find(plantao.getId());

                if (realizado != null) {
                    realizado.setDataInicio(plantao.getDataInicio());
                    realizado.setDataFim(plantao.getDataFim());
                    realizado.setApontadorEntrada(usuarioExecutante);
                    realizado.setApontadorSaida(usuarioExecutante);

                    plantaoRealizadoDAO.update(realizado);
                }
            }

            plantaoPlanejadoDAO.update(plantao);

            List<Long> ids = new ArrayList<>();
            ids.add(id);
            processadorHistorico.fire(new ProcessadorHistoricoEvent(ids, PlantaoHistoricoEnum.PLANTAO_ALTERADO, PlantaoHistoricoOrigemEnum.PLANTAO_ORIGEM_PLANEJAMENTO));
        } else {
            retorno.add(new ChaveValorVO("PLANTAO", "Erro ao alterar a data. Plantão possui indicações."));
        }

        result.use(Results.json()).withoutRoot().from(retorno).serialize();
    }

    @Post("/agendamento/calendario/buscarProfissional")
    public void buscarProfissional(String consulta, Long idLocal) throws PreventSeniorException {
        List<ProfissionalPrestadorVO> retorno = new ArrayList<>();
        consulta = consulta.trim();
        if (consulta.length() >= 3) {
            retorno = profissionalVigenciaDAO.buscarProfissionaisVigencia(consulta, idLocal);
        }
        result.use(Results.json()).withoutRoot().from(retorno).serialize();
    }

    @Post("/agendamento/calendario/buscarTiposDePlantao")
    public void buscarTiposDePlantao(Long inicio, Long idLocal, Long idProfissional) throws DAOException {

        Date dtInicio = new DateTime().minusMonths(1).withDayOfMonth(1).withTimeAtStartOfDay().toDate();

        if (inicio != null) {
            dtInicio = new Date(inicio);
        }

        List<TipoPlantaoLocalAtendimento> tipos = profissionalVigenciaDAO.buscarTiposDePlantaoDoPeriodo(idLocal, idProfissional, dtInicio);

        result.use(Results.json()).withoutRoot().from(tipos).serialize();
    }

    @Post("/agendamento/calendario/buscarEspecialidades")
    public void buscarEspecialidades(Long idEspecialidade) throws DAOException {

        List<SubEspecialidade> subEspecialidades = subEspecialidadeRemote.buscarSubEspecialidadesDaEspecialidade(idEspecialidade);
        result.use(Results.json()).withoutRoot().from(subEspecialidades).include("especialidade").serialize();
    }

    @Transactional
    @Post("/agendamento/calendario/checarAgendamentoNoPeriodo")
    public void checarAgendamentoPeriodo(Long inicio, Long fim, Long idProfissional, Long idPlantao, Long idLocalAtendimento) throws DAOException {
        List<ChaveValorVO> retorno = new ArrayList<>();

        boolean temIndicacao = indicacaoAtendimentoRemote.temIndicacoesAtivasForaData(idPlantao, new Date(inicio), new Date(fim));

        if (temIndicacao) {
            retorno.add(new ChaveValorVO("PLANTAO", "Este plantão possuí indicações, alteração de horário cancelada."));
        }

        AtendimentoPeriodoVO checagemPlantao = checarAgendamentoNoPeriodo(inicio, fim, idProfissional, idPlantao);
        boolean configuracaoTipoPlantao = false;

        LocalAtendimentoPlantao local = localAtendimentoPlantaoDAO.find(idLocalAtendimento);

        PlantaoPlanejado planejado = plantaoPlanejadoDAO.find(idPlantao);

        DateTime agora = new DateTime();

        DateTime inicioPlantao = new DateTime(planejado.getDataInicio());

        DateTime novoInico = new DateTime(inicio);

        if (novoInico.isBefore(agora)) {
            retorno.add(new ChaveValorVO("PLANTAO", "Horário desejado é anterior ao horário atual, não pode ser alterado"));
        }

        if (agora.isAfter(inicioPlantao)) {
            retorno.add(new ChaveValorVO("PLANTAO", "Este plantão já está em andamento, não pode ser alterado"));
        }

        if (checagemPlantao.isTemAgendamento()) {
            retorno.add(new ChaveValorVO("PLANTAO", "Já existe um plantão para esse profissional no local " + checagemPlantao.getNomeLocal()));
        }

        PlantaoPlanejado plantaoPlanejado = plantaoPlanejadoDAO.find(idPlantao);

        if (plantaoPlanejado.getPausas() != null && !plantaoPlanejado.getPausas().isEmpty()) {
            retorno.add(new ChaveValorVO("PAUSAS", "Este plantão somente pode ser alterado pela tela de edição."));
        }

        List<TipoPlantaoLocalAtendimento> tiposPlantao = profissionalVigenciaDAO.buscarTiposDePlantaoDoPeriodo(local.getId(), planejado.getProfissional().getId(), new Date(inicio));

        for (TipoPlantaoLocalAtendimento tipoPlantao : tiposPlantao) {
            if (tipoPlantao.getId().equals(planejado.getTipoPlantao().getId())) {
                configuracaoTipoPlantao = true;
            }
        }

        if (!configuracaoTipoPlantao) {
            retorno.add(new ChaveValorVO("TIPO_PLANTAO", "Este plantão não possuí configuração válida de tipo de plantão para esse período."));
        }

        result.use(Results.json()).withoutRoot().from(retorno).serialize();
    }

    @Transactional
    @Post("/agendamento/calendario/checarEvento")
    public void checarAgendamento(String inicio, String fim, Long idProfissional, Long idPlantao, Long idLocalAtendimento, Long idTipoPlantao, Long idTipoMarcacao, Integer duracao, String[] horariosExtras, String cnpjLocal) throws DAOException, ParseException {

        /*** ESSE TRECHO FOI ADICIONADO PARA ARRUMAR O ENVIO ERRADO DE GMT PELO MOMENT NO FORMULARIO ***/
        Date dtInicio = sdfCompleto.parse(inicio);
        Date dtFim = sdfCompleto.parse(fim);

        Calendar calInicio = Calendar.getInstance();
        calInicio.setTime(dtInicio);
        Calendar calFim = Calendar.getInstance();
        calFim.setTime(dtFim);

        Date agora = new Date();
        List<ChaveValorVO> retorno = new ArrayList<>();

        boolean temIndicacaoForaData = false;
        boolean temIndicacao = false;
        List<Date> datasExtras = new ArrayList<Date>();

        // Se os dias entre inicio e fim forem diferntes
        if (horariosExtras.length > 0 && horariosExtras[0] != null && calInicio.get(Calendar.DAY_OF_MONTH) != calFim.get(Calendar.DAY_OF_MONTH)) {
            for (String horarioExtra : horariosExtras) {
                int hour = Integer.parseInt(horarioExtra.split(":")[0].trim());
                int minute = Integer.parseInt(horarioExtra.split(":")[1].trim());

                Calendar horarioExtraEntrada = Calendar.getInstance();
                horarioExtraEntrada.setTime(dtInicio);
                horarioExtraEntrada.set(Calendar.HOUR_OF_DAY, hour);
                horarioExtraEntrada.set(Calendar.MINUTE, minute);

                Calendar horarioExtraFim = Calendar.getInstance();
                horarioExtraFim.setTime(dtFim);
                horarioExtraFim.set(Calendar.HOUR_OF_DAY, hour);
                horarioExtraFim.set(Calendar.MINUTE, minute);


                if (horarioExtraEntrada.before(calInicio) && horarioExtraFim.after(calFim)) {
                    retorno.add(new ChaveValorVO("PLANTAO", "O horário extra " + horarioExtra + " inválido."));
                } else if (!horarioExtraEntrada.before(calInicio)) {
                    datasExtras.add(horarioExtraEntrada.getTime());
                } else if (!horarioExtraFim.after(calFim)) {
                    datasExtras.add(horarioExtraFim.getTime());
                }
            }
        } else if (horariosExtras.length > 0 && horariosExtras[0] != null) { // Se os dias forem os mesmos
            for (String horarioExtra : horariosExtras) {
                int hour = Integer.parseInt(horarioExtra.split(":")[0].trim());
                int minute = Integer.parseInt(horarioExtra.split(":")[1].trim());

                Calendar horarioExtraEntrada = Calendar.getInstance();
                horarioExtraEntrada.setTime(dtInicio);
                horarioExtraEntrada.set(Calendar.HOUR_OF_DAY, hour);
                horarioExtraEntrada.set(Calendar.MINUTE, minute);

                if (horarioExtraEntrada.before(calInicio) || horarioExtraEntrada.after(calFim)) {
                    retorno.add(new ChaveValorVO("PLANTAO", "O horário extra " + horarioExtra + " inválido."));
                } else {
                    datasExtras.add(horarioExtraEntrada.getTime());
                }
            }
        }

        List<Date> horariosExtrasComAgendamento = indicacaoAtendimentoRemote.validarListaHorarioExtraDoPlantao(idPlantao, datasExtras);

        if (horariosExtrasComAgendamento != null && !horariosExtrasComAgendamento.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Existe um agendamento para os horários extras abaixo:<br>");
            for (Date horarioExtraAgendado : horariosExtrasComAgendamento) {
                sb.append(sdfHour.format(horarioExtraAgendado));
                sb.append("<br>");
            }

            retorno.add(new ChaveValorVO("EXTRA", sb.toString()));
        }

        AtendimentoPeriodoVO checagemPlantao = checarAgendamentoNoPeriodo(dtInicio.getTime(), dtFim.getTime(), idProfissional, idPlantao);

        if (dtFim.before(dtInicio) || dtFim.equals(dtInicio)) {
            retorno.add(new ChaveValorVO("FIM", "A Data fim é igual ou inferior a data de início."));
        } else if (checagemPlantao.isTemAgendamento()) {
            retorno.add(new ChaveValorVO("PLANTAO", "Já existe um plantão para esse profissional no local: <br><b>" + checagemPlantao.getNomeLocal() + "</b>"));
        }

        if (dtInicio.before(agora) && idPlantao == 0) {
            retorno.add(new ChaveValorVO("INICIO", "Não é pertimido criar um plantão para um dia/horário antigo."));
        }

        if (idPlantao != 0) {
            temIndicacaoForaData = indicacaoAtendimentoRemote.temIndicacoesAtivasForaData(idPlantao, dtInicio, dtFim);
            temIndicacao = this.indicacaoAtendimentoRemote.temIndicacoesAtivas(idPlantao);

            PlantaoPlanejado pp = this.plantaoPlanejadoDAO.find(idPlantao);

            if (temIndicacaoForaData) {
                retorno.add(new ChaveValorVO("PLANTAO", "Este plantão possuí indicações, alteração de horário cancelada."));
            }

            if (pp.getTipoMarcacao() != null && idTipoMarcacao == null && temIndicacao) {
                retorno.add(new ChaveValorVO("MARCACAO", "Esse plantão possui indicações, alteração de marcação cancelada"));
            }

            if (pp.getTipoMarcacao() != null && (idTipoMarcacao != null && pp.getTipoMarcacao() != TipoMarcacaoEnum.get(idTipoMarcacao) && temIndicacao) || (idTipoMarcacao != null && pp.getTipoMarcacao() != TipoMarcacaoEnum.get(idTipoMarcacao) && temIndicacaoForaData)) {
                retorno.add(new ChaveValorVO("MARCACAO", "Esse plantão possui indicações, alteração de marcação cancelada"));
            }

            if (pp.getDuracaoConsulta() != null && duracao != null && duracao != pp.getDuracaoConsulta().getCodigo() && temIndicacao) {
                retorno.add(new ChaveValorVO("MARCACAO", "Esse plantão possui indicações, alteração de marcação cancelada"));
            }

            if (temIndicacaoForaData && dtFim.before(plantaoPlanejadoDAO.buscarDataFimPorId(idPlantao))) {
                retorno.add(new ChaveValorVO("PLANTAO", "Este plantão possuí indicações, alteração de horário cancelada."));
            }

            if (dtInicio.before(agora) && !dtInicio.equals(plantaoPlanejadoDAO.buscarDataInicioPorId(idPlantao))) {
                retorno.add(new ChaveValorVO("INICIO", "A Data de ínicio é anterior a data atual."));
            }

            if (dtFim.before(agora) || dtFim.equals(agora)) {
                retorno.add(new ChaveValorVO("FIM", "A Data fim é igual ou inferior a data atual."));
            }
        }
        result.use(Results.json()).withoutRoot().from(retorno).serialize();
    }

    public AtendimentoPeriodoVO checarAgendamentoNoPeriodo(Long inicio, Long fim, Long idProfissional, Long idPlantao) throws DAOException {
        List<PlantaoPlanejado> plantoes = plantaoPlanejadoDAO.checarSeExisteAgendamento(new DateTime(inicio).plusSeconds(1).toDate(), new DateTime(fim).minusSeconds(1).toDate(), idProfissional, idPlantao);

        AtendimentoPeriodoVO retorno = new AtendimentoPeriodoVO();

        if (plantoes.size() > 0) {
            retorno.setTemAgendamento(true);

            String inicioPP = sdf.format(plantoes.get(0).getDataInicio());
            String fimPP = sdf.format(plantoes.get(0).getDataFim());
            DivisaoOperacional divisaoOperacional = plantoes.get(0).getLocalAtendimento().getDivisaoOperacional();

            if(divisaoOperacional == null) {
                retorno.setNomeLocal(plantoes.get(0).getLocalAtendimento().getLocalAtendimento().getApelido() + " (" + inicioPP + " à " + fimPP + ")");
            } else{
                retorno.setNomeLocal(plantoes.get(0).getLocalAtendimento().getLocalAtendimento().getApelido().concat(" (" + divisaoOperacional.getNome() + ") ") + "<br> (" + inicioPP + " à " + fimPP + ")");
            }
        }

        return retorno;
    }

    @Post("/agendamento/calendario/verificarSeTemIndicacao")
    public void verificarSeTemIndicacao(Long idPlantao) throws DAOException {

        boolean temIndicacao = indicacaoAtendimentoRemote.temIndicacoesAtivas(idPlantao);

        result.use(Results.json()).withoutRoot().from(temIndicacao).serialize();
    }

    private PlantaoPlanejado atualizaHorariosExtras(PlantaoPlanejado pp) throws DAOException {
        List<PlantaoPlanejadoHoraExtra> horasExtras = pp.getHorasExtras();
        if (!horasExtras.isEmpty()) {
            Calendar calInicio = Calendar.getInstance();
            calInicio.setTime(pp.getDataInicio());
            Calendar calFim = Calendar.getInstance();
            calFim.setTime(pp.getDataFim());
            List<PlantaoPlanejadoHoraExtra> novasHrExtras = new ArrayList<>();

            for (PlantaoPlanejadoHoraExtra hr : horasExtras) {
                Calendar calHr = Calendar.getInstance();
                calHr.setTime(hr.getId().getHorarioExtra());

                // Se os dias, entre inicio e fim do plantão, forem diferentes
                if (calInicio.get(Calendar.DAY_OF_MONTH) != calFim.get(Calendar.DAY_OF_MONTH)) {
                    Calendar horarioExtraNaEntrada = (Calendar) calInicio.clone();
                    horarioExtraNaEntrada.set(Calendar.HOUR_OF_DAY, calHr.get(Calendar.HOUR_OF_DAY));
                    horarioExtraNaEntrada.set(Calendar.MINUTE, calHr.get(Calendar.MINUTE));

                    // Se o horário extra, no dia inicial, for menor do que o dia e hora inicial, então este horário refere-se ao dia do fim
                    if (horarioExtraNaEntrada.getTime().before(calInicio.getTime())) {
                        calHr.set(Calendar.YEAR, calFim.get(Calendar.YEAR));
                        calHr.set(Calendar.MONTH, calFim.get(Calendar.MONTH));
                        calHr.set(Calendar.DAY_OF_MONTH, calFim.get(Calendar.DAY_OF_MONTH));
                    } else {
                        calHr.set(Calendar.YEAR, calInicio.get(Calendar.YEAR));
                        calHr.set(Calendar.MONTH, calInicio.get(Calendar.MONTH));
                        calHr.set(Calendar.DAY_OF_MONTH, calInicio.get(Calendar.DAY_OF_MONTH));
                    }
                    novasHrExtras.add(new PlantaoPlanejadoHoraExtra(new PlantaoPlanejadoHoraExtraPK(pp, calHr.getTime())));

                } else {
                    calHr.set(Calendar.YEAR, calInicio.get(Calendar.YEAR));
                    calHr.set(Calendar.MONTH, calInicio.get(Calendar.MONTH));
                    calHr.set(Calendar.DAY_OF_MONTH, calInicio.get(Calendar.DAY_OF_MONTH));
                    novasHrExtras.add(new PlantaoPlanejadoHoraExtra(new PlantaoPlanejadoHoraExtraPK(pp, calHr.getTime())));
                }

            }
            plantaoPlanejadoHoraExtraDAO.deletarTodasHorasExtrasPlantaoPlanejado(pp.getId());
            pp.setHorasExtras(novasHrExtras);
        }
        return pp;
    }

    private PlantaoPlanejado converterParaPlantao(EventoCalendarioVO calendario, List<PausaPlantaoPlanejado> pausas, String[] horariosExtras) throws DAOException, PreventSeniorException, ParseException {

        Usuario usuarioExecutante = new Usuario();
        usuarioExecutante.setId(usuario.getId());
        Prestador empresa = profissionalVigenciaDAO.buscaPrestadorPorProfissionalTipoLocalAtdm(calendario.getIdProfissional(), calendario.getIdTipoPlantao(), calendario.getStartDate());

        PlantaoPlanejado plantao;

        if (calendario.getId() != null) {
            plantao = new PlantaoPlanejado();
            PlantaoPlanejado plantaoDb = plantaoPlanejadoDAO.find(calendario.getId());
            plantao.setDataCriacao(plantaoDb.getDataCriacao());
        } else {
            plantao = new PlantaoPlanejado();
            plantao.setDataCriacao(new Date());
        }

        /*** ESSE TRECHO FOI ADICIONADO PARA ARRUMAR O ENVIO ERRADO DE GMT PELO MOMENT NO FORMULARIO ***/
        Date dtInicio = sdfSeparado.parse(calendario.getStart());
        Date dtFim = sdfSeparado.parse(calendario.getEnd());

        Calendar calInicio = Calendar.getInstance();
        calInicio.setTime(calendario.getStartDate());
        Calendar calFim = Calendar.getInstance();
        calFim.setTime(calendario.getEndDate());

        ArrayList<Date> datasExtras = new ArrayList<Date>();

        // Se os dias, entre inicio e fim do plantão, forem diferentes
        if (calInicio.get(Calendar.DAY_OF_MONTH) != calFim.get(Calendar.DAY_OF_MONTH)) {
            for (String horarioExtra : horariosExtras) {
                int hour = Integer.parseInt(horarioExtra.split(":")[0]);
                int minute = Integer.parseInt(horarioExtra.split(":")[1]);

                Calendar horaExtra = Calendar.getInstance();

                Calendar horarioExtraNaEntrada = (Calendar) calInicio.clone();
                horarioExtraNaEntrada.set(Calendar.HOUR_OF_DAY, hour);
                horarioExtraNaEntrada.set(Calendar.MINUTE, minute);

                // Se o horário extra, no dia inicial, for menor do que o dia e hora inicial, então este horário refere-se ao dia do fim
                if (horarioExtraNaEntrada.getTime().before(dtInicio)) {
                    horaExtra.setTime(dtFim);
                } else {
                    horaExtra.setTime(dtInicio);
                }
                horaExtra.set(Calendar.HOUR_OF_DAY, hour);
                horaExtra.set(Calendar.MINUTE, minute);
                datasExtras.add(horaExtra.getTime());
            }
        } else { // A data inicio e fim são as mesmas
            for (String horarioExtra : horariosExtras) {
                int hour = Integer.parseInt(horarioExtra.split(":")[0]);
                int minute = Integer.parseInt(horarioExtra.split(":")[1]);

                Calendar horarioExtraNaEntrada = Calendar.getInstance();
                horarioExtraNaEntrada.setTime(dtInicio);
                horarioExtraNaEntrada.set(Calendar.HOUR_OF_DAY, hour);
                horarioExtraNaEntrada.set(Calendar.MINUTE, minute);

                datasExtras.add(horarioExtraNaEntrada.getTime());
            }
        }

        List<PlantaoPlanejadoHoraExtra> dataHoraExtra = new ArrayList<PlantaoPlanejadoHoraExtra>();
        for (Date extra : datasExtras) {
            PlantaoPlanejadoHoraExtraPK horaExtraPK = new PlantaoPlanejadoHoraExtraPK(plantao, extra);
            PlantaoPlanejadoHoraExtra horaExtra = new PlantaoPlanejadoHoraExtra(horaExtraPK);
            dataHoraExtra.add(horaExtra);
        }

        plantao.setHorasExtras(dataHoraExtra);

        List<PlantaoEspecialidade> especialidades = new ArrayList<>();

        List<Categoria> categorias = new ArrayList<Categoria>();
        for (Long idCategoria : calendario.getCategorias()) {
            categorias.add(new Categoria(idCategoria));
        }


        if (calendario.getGrupos() != null) {

            List<PlantaoPlanejadoGrupoMarcacao> grupos = new ArrayList<>();

            for (Long idGrupo : calendario.getGrupos()) {
                GrupoMarcacao grupo = new GrupoMarcacao();
                grupo.setId(idGrupo);

                grupos.add(new PlantaoPlanejadoGrupoMarcacao(plantao, grupo));

            }

            plantao.setGrupos(grupos);

        }


        plantao.setId(calendario.getId());
        plantao.setCor(calendario.getClassColor());
        plantao.setDataInicio(calendario.getStartDate());
        plantao.setDataFim(calendario.getEndDate());
        plantao.setIntervalo(0);
        plantao.setSala(calendario.getSala());
        plantao.setCategorias(categorias);


        TipoPlantaoLocalAtendimento tipoPlantao = new TipoPlantaoLocalAtendimento();
        tipoPlantao.setId(calendario.getIdTipoPlantao());

        LocalAtendimentoPlantao local = localAtendimentoPlantaoDAO.find(calendario.getIdLocalAtendimento());

        plantao.setLocalAtendimento(local);

        plantao.setTipoPlantao(tipoPlantao);
        plantao.setNome(calendario.getTitle());
        plantao.setObservacao(calendario.getObservacao());
        plantao.setUsuario(usuario.getId());

        if (calendario.getIdTipoMarcacao() != null && calendario.getIdTipoMarcacao().equals(TipoMarcacaoEnum.AGENDAMENTO.getId())) {
            plantao.setDuracaoConsulta(DuracaoConsultaEnum.get(calendario.getDuracaoConsulta()));
        } else {
            plantao.setDuracaoConsulta(null);
            plantao.setTipoMarcacao(null);
        }

        plantao.setTipoAtendimento(TipoAtendimentoEnum.valueOf(calendario.getTipoAtendimento()));

        if (calendario.getIdProfissional() != null) {

            Profissional profissional = new Profissional();
            profissional = profissionalRemote.buscarPorId(calendario.getIdProfissional());
            plantao.setProfissional(profissional);

        }

        if (calendario.getIdProfissionalResponsavel() != null) {

            Profissional profissional = new Profissional();
            profissional = profissionalRemote.buscarPorId(calendario.getIdProfissionalResponsavel());
            plantao.setProfissionalResponsavel(profissional);

        }

        if (calendario.getIdTipoMarcacao() != null) {
            plantao.setTipoMarcacao(TipoMarcacaoEnum.get(calendario.getIdTipoMarcacao()));
        }

        for (Long idEspecialidade : calendario.getEspecialidades()) {
            PlantaoEspecialidade especialidade = new PlantaoEspecialidade();
            especialidade.setPlantao(plantao);
            SubEspecialidade subEsp = new SubEspecialidade();
            subEsp.setId(idEspecialidade);
            especialidade.setSubespecialidade(subEsp);
            especialidades.add(especialidade);
        }

        for (PausaPlantaoPlanejado pausa : pausas) {
            if (pausa.getId() != null) {
                pausa.setId(pausa.getId() == 0l ? null : pausa.getId());
            }
            pausa.setPlantaoPlanejado(plantao);
        }

        if (calendario.getAtivo()) {
            plantao.setStatus(StatusPlantaoEnum.ATIVO);
        } else {
            plantao.setStatus(StatusPlantaoEnum.OCULTO);
        }

        plantao.setPausas(pausas);

        plantao.setEspecialidades(especialidades);

        if (!local.isApontamentoManual()) {

            PlantaoRealizado realizado = new PlantaoRealizado();
            realizado.setId(plantao.getId());

            realizado.setApontadorEntrada(usuarioExecutante);
            realizado.setApontadorSaida(usuarioExecutante);
            realizado.setDataInicio(plantao.getDataInicio());
            realizado.setDataFim(plantao.getDataFim());
            realizado.setStatus(SituacaoPlantaoEnum.ENCERRADO);
            realizado.setPlantaoPlanejado(plantao);

            PlantaoAprovado aprovado = new PlantaoAprovado();
            aprovado.setId(plantao.getId());
            aprovado.setDataAprovacao(new Date());
            aprovado.setDataLiberacaoPagamento(new Date());
            aprovado.setDataInicio(plantao.getDataInicio());
            aprovado.setDataFim(plantao.getDataFim());
            aprovado.setPlantaoRealizado(realizado);
            aprovado.setUsuario(usuarioExecutante);
            aprovado.setSituacaoIntegracaoLegado(SituacaoIntegracaoLegadoEnum.AGUARDANDO);
            aprovado.setEmpresa(empresa);

            List<PausaPlantaoRealizado> pausasRealizadas = new ArrayList<>();
            List<PausaPlantaoAprovacao> pausasAprovadas = new ArrayList<>();
            if (plantao.getPausas() != null && !plantao.getPausas().isEmpty()) {
                for (PausaPlantaoPlanejado pausaPlanejada : plantao.getPausas()) {
                    PausaPlantaoRealizado pausaRealizada = new PausaPlantaoRealizado();
                    pausaRealizada.setDataInicio(pausaPlanejada.getDataInicio());
                    pausaRealizada.setDataFim(pausaPlanejada.getDataFim());
                    pausaRealizada.setApontadorEntrada(usuarioExecutante);
                    pausaRealizada.setApontadorSaida(usuarioExecutante);
                    pausaRealizada.setPlantaoRealizado(realizado);
                    pausasRealizadas.add(pausaRealizada);
                }

                for (PausaPlantaoPlanejado pausaPlanejada : plantao.getPausas()) {
                    PausaPlantaoAprovacao pausaAprovada = new PausaPlantaoAprovacao();
                    pausaAprovada.setDataFim(pausaPlanejada.getDataFim());
                    pausaAprovada.setDataInicio(pausaPlanejada.getDataInicio());
                    pausaAprovada.setPlantaoAprovado(aprovado);
                    pausasAprovadas.add(pausaAprovada);
                }
            }

            realizado.setPausas(pausasRealizadas);
            aprovado.setPausas(pausasAprovadas);

            plantao.setPlantaoRealizado(realizado);
            realizado.setPlantaoAprovado(aprovado);

            calculadorValorPlantao.calcularApontamentoAutomatico(aprovado, calendario.getIdProfissional(), calendario.getIdLocalAtendimento());

        }

        return plantao;
    }

    @Transactional
    @Path("/agendamento/calendario/criarAgendamentoMultiplo")
    public void criarAgendamentoMultiplo(Long idLocal, Boolean retroativo) throws DAOException, ParseException {

        LocalAtendimentoPlantao local = localAtendimentoPlantaoDAO.find(idLocal);
        Date ultimaCompetencia = controleExtratoPagamentoDetalhadoDAO.verificarUltimaCompetencia();
        List<Especialidade> especialidades = especialidadeRemote.listar();
        result.include("especialidades", especialidades);
        result.include("tiposAtendimento", TipoAtendimentoEnum.values());
        result.include("local", local);
        result.include("mesFechamento", ultimaCompetencia != null ? new DateTime(ultimaCompetencia).getMonthOfYear() : null);
        result.include("anoFechamento", ultimaCompetencia != null ? new DateTime(ultimaCompetencia).getYear() : null);
        result.include("retroativo", retroativo == null ? false : true);
        result.include("inicioCalendario", sdfMes.format(ultimaCompetencia));
    }

    @Transactional
    @Path({"/agendamento/calendario/editarAgendamento/multiplo/{idEvento}", "/agendamento/calendario/editarAgendamento/multiplo/{idEvento}/{tela}"})
    public void editarAgendamentoMultiplo(Long idEvento, String tela) throws DAOException, PreventSeniorException {

        PlantaoPlanejado agendamento = plantaoPlanejadoDAO.find(idEvento);

        if (agendamento != null) {

            List<Especialidade> especialidades = especialidadeRemote.listar();
            result.include("especialidades", especialidades);
            result.include("tiposAtendimento", TipoAtendimentoEnum.values());
            result.include("idPlantao", idEvento);
            result.include("tela", tela);

            result.forwardTo("/WEB-INF/jsp/agendamentoPlantao/criarAgendamentoMultiplo.jsp");
        } else {
            result.forwardTo("/WEB-INF/jsp/compartilhados/plantaoNaoEncontrado.jsp");
        }

    }

    @Transactional
    @Post("/agendamento/calendario/editarAgendamento/multiplo/buscarDadosPlantao")
    public void buscarDadosPlantao(Long idPlantao, String abertoPelaTela) throws DAOException, ParseException {
        PlantaoPlanejado plantaoPlanejado = plantaoPlanejadoDAO.find(idPlantao);

        boolean unidadeAntecipacao = plantaoPlanejado.getLocalAtendimento().getTipoPagamentoUnidade().getId().equals(TipoPagamentoUnidadeEnum.ANTECIPACAO_DE_PAGAMENTO.getCodigo());
        Interval interval = null;

        Date ultimoFechamento = controleExtratoPagamentoFechamentoDAO.buscarUltimoFechamento();
        if (unidadeAntecipacao) {
            Date dtFechamento = controleExtratoPagamentoDetalhadoDAO.verificarUltimaCompetencia();


            if (dtFechamento != null) {
                Date inicio = dateUtils.pegarOInicioDoDia(sdf.format(dtFechamento));
                Date fim = dateUtils.pegarOUltimoDiaDoMes(sdf.format(dtFechamento));
                //new DateTime(new DateTime(dtFechamento).dayOfMonth().withMaximumValue().withTime(23, 59, 59, 999));

                interval = new Interval(new DateTime(inicio), new DateTime(fim));
            }
        }

        PlantaoPlanejadoVO vo = new PlantaoPlanejadoVO(plantaoPlanejado, abertoPelaTela, unidadeAntecipacao, interval, ultimoFechamento);

        result.use(Results.json()).withoutRoot().from(vo).include("tiposPlantao").include("idsSubEspecialidades").serialize();
    }

    @Transactional
    @Consumes("application/json")
    @Post("/agendamento/calendario/salvarEventoMultiplo")
    public void salvarEventoMultiplo(PlantaoHospitalVO dados) throws Exception {
        if (dados.getIdPlantao() == null) {
            agendamentoPlantaoBusiness.criarplantoes(dados);
        } else {
            agendamentoPlantaoBusiness.editarPlantao(dados);
        }

        result.use(Results.json()).withoutRoot().from("OK").serialize();
    }

    @Consumes("application/json")
    @Post("/agendamento/calendario/validarPlantoes")
    @Transactional
    public void validarPlantoes(PlantaoHospitalVO dados) throws Exception {
        if (dados.getDuracao() != null) {
            List<PlantoesValidadosVO> retorno = agendamentoPlantaoBusiness.validarPlantoes(dados);
            result.use(Results.json()).withoutRoot().from(retorno).recursive().serialize();
        }
    }

    @Consumes("application/json")
    @Post("/agendamento/calendario/prepararDatas")
    public void prepararDatas(ValidacaoHorasVO dados) {

        java.util.Locale locale = new java.util.Locale( "pt", "BR" );

        List<ChaveValorDTO<Date,String>> periodos = new ArrayList<>();
        DateTimeFormatter diaSemHora = DateTimeFormat.forPattern("dd/MM/yyyy").withLocale( locale );
        DateTimeFormatter diaComHora = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm").withLocale( locale );
        DateTimeFormatter diaBase = DateTimeFormat.forPattern("dd/MM/yyyy - EEEE").withLocale( locale );
        DateTimeFormatter horaBase = DateTimeFormat.forPattern("HH:mm").withLocale( locale );

        for (String d: dados.getDias()) {
            DateTime dia = null;

            if(Strings.isNullOrEmpty(dados.getInicio())){
                dia = diaSemHora.parseDateTime(d);
                periodos.add(new ChaveValorDTO<Date,String>(dia.toDate(),dia.toString(diaBase)));
            }
            else{
                dia = diaComHora.parseDateTime(d + " " + dados.getInicio());

                if(Strings.isNullOrEmpty(dados.getPeriodo())){
                    periodos.add(new ChaveValorDTO<Date,String>(dia.toDate(),dia.toString(diaBase) + " (" + dia.toString(horaBase)+ ")"));
                    //periodos.add(dia.toString(diaBase) + " (" + dia.toString(horaBase)+ ")");
                }
                else{
                    String [] periodo = dados.getPeriodo().split(":");

                    int periodoHora = Integer.parseInt(periodo[0]);
                    int periodoMinutos = Integer.parseInt(periodo[1]);
                    DateTime fimPlantao = dia.plusHours(periodoHora).plusMinutes(periodoMinutos);

                    boolean sameDay = org.apache.commons.lang3.time.DateUtils.isSameDay(dia.toDate(), fimPlantao.toDate());

                    if(sameDay){

                        periodos.add(new ChaveValorDTO<Date,String>(dia.toDate(),dia.toString(diaBase) + " (" + dia.toString(horaBase) + " às "  + fimPlantao.toString(horaBase) + ")"));
                        //periodos.add(dia.toString(diaBase) + " (" + dia.toString(horaBase) + " às "  + fimPlantao.toString(horaBase) + ")");
                    }
                    else{
                        periodos.add(new ChaveValorDTO<Date,String>(dia.toDate(),dia.toString(diaBase) + " (" + dia.toString(horaBase) + ") até "  + fimPlantao.toString(diaBase) + " (" + fimPlantao.toString(horaBase) + ")"));
                        //periodos.add(dia.toString(diaBase) + " (" + dia.toString(horaBase) + ") até "  + fimPlantao.toString(diaBase) + " (" + fimPlantao.toString(horaBase) + ")");
                    }
                }

            }

        }

        Collections.sort(periodos, new Comparator<ChaveValorDTO<Date,String>>() {
            @Override
            public int compare(ChaveValorDTO<Date,String> o1, ChaveValorDTO<Date,String> o2) {
                return o1.getChave().compareTo(o2.getChave());
            }
        });

        result.use(Results.json()).withoutRoot().from(periodos).recursive().serialize();
    }




}
