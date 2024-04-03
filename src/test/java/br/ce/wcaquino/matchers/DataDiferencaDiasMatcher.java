package br.ce.wcaquino.matchers;

import br.ce.wcaquino.utils.DataUtils;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.Date;

public class DataDiferencaDiasMatcher extends TypeSafeMatcher<Date> {
    public Integer qtdeDias;

    public DataDiferencaDiasMatcher(Integer qtdeDias) {
        this.qtdeDias = qtdeDias;
    }

    public void describeTo(Description desc) {

    }

    @Override
    protected boolean matchesSafely(Date data) {
        return DataUtils.isMesmaData(data, DataUtils.obterDataComDiferencaDias(qtdeDias));
    }
}
