package com.example.nexacro.excel;

import com.example.nexacro.dto.CodeMst;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class ComboResolverTest {

    private ComboResolver resolver;

    @BeforeEach
    void setUp() {
        List<CodeMst> codes = Arrays.asList(
            code("REGION", "SEO", "서울"),
            code("REGION", "BUS", "부산"),
            code("CATEGORY", "ELEC", "전자제품"),
            code("CATEGORY", "CLOT", "의류")
        );
        resolver = new ComboResolver(codes);
    }

    @Test
    void resolves_known_code_to_text() {
        assertThat(resolver.resolve("REGION", "SEO")).isEqualTo("서울");
        assertThat(resolver.resolve("CATEGORY", "ELEC")).isEqualTo("전자제품");
    }

    @Test
    void returns_code_as_fallback_when_not_found() {
        assertThat(resolver.resolve("REGION", "UNKNOWN")).isEqualTo("UNKNOWN");
        assertThat(resolver.resolve("NO_GROUP", "SEO")).isEqualTo("SEO");
    }

    @Test
    void returns_null_when_code_value_is_null() {
        assertThat(resolver.resolve("REGION", null)).isNull();
    }

    @Test
    void returns_code_when_group_is_null() {
        assertThat(resolver.resolve(null, "SEO")).isEqualTo("SEO");
    }

    private CodeMst code(String groupCd, String code, String codeNm) {
        CodeMst c = new CodeMst();
        c.setGroupCd(groupCd);
        c.setCode(code);
        c.setCodeNm(codeNm);
        return c;
    }
}
