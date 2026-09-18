package io.github.othmaneataallah.problemdetails.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ProblemDetailTest {

  @Test
  void builderDefaultsToAboutBlankWithAbsentMembers() {
    ProblemDetail problem = ProblemDetail.builder().build();

    assertThat(problem.getType()).isEqualTo(URI.create("about:blank"));
    assertThat(problem.getTitle()).isNull();
    assertThat(problem.getStatus()).isNull();
    assertThat(problem.getDetail()).isNull();
    assertThat(problem.getInstance()).isNull();
    assertThat(problem.extensionMembers()).isEmpty();
  }

  @Test
  void aboutBlankConstantMatchesDefaultType() {
    assertThat(URI.create("about:blank")).isEqualTo(ProblemDetail.ABOUT_BLANK);
    assertThat(ProblemDetail.builder().build().getType()).isEqualTo(ProblemDetail.ABOUT_BLANK);
  }

  @Test
  void allMembersRoundTrip() {
    ProblemDetail problem =
        ProblemDetail.builder()
            .type("https://example.com/probs/out-of-credit")
            .title("You do not have enough credit.")
            .status(403)
            .detail("Your current balance is 30, but that costs 50.")
            .instance("/account/12345/msgs/abc")
            .build();

    assertThat(problem.getType()).isEqualTo(URI.create("https://example.com/probs/out-of-credit"));
    assertThat(problem.getTitle()).isEqualTo("You do not have enough credit.");
    assertThat(problem.getStatus()).isEqualTo(403);
    assertThat(problem.getDetail()).isEqualTo("Your current balance is 30, but that costs 50.");
    assertThat(problem.getInstance()).isEqualTo(URI.create("/account/12345/msgs/abc"));
  }

  @Test
  void uriOverloadsAcceptUriInstances() {
    ProblemDetail problem =
        ProblemDetail.builder()
            .type(URI.create("https://example.com/probs/out-of-credit"))
            .instance(URI.create("/account/12345/msgs/abc"))
            .build();

    assertThat(problem.getType()).isEqualTo(URI.create("https://example.com/probs/out-of-credit"));
    assertThat(problem.getInstance()).isEqualTo(URI.create("/account/12345/msgs/abc"));
  }

  @Test
  void invalidUriReferenceIsRejected() {
    assertThatThrownBy(() -> ProblemDetail.builder().type("not a uri \\ue007"))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> ProblemDetail.builder().instance("not a uri \\ue007"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void nullTypeAndInstanceAreRejected() {
    assertThatThrownBy(() -> ProblemDetail.builder().type((URI) null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> ProblemDetail.builder().type((String) null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> ProblemDetail.builder().instance((URI) null))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> ProblemDetail.builder().instance((String) null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void titleAndDetailMayBeExplicitlyNull() {
    ProblemDetail problem = ProblemDetail.builder().title(null).detail(null).build();

    assertThat(problem.getTitle()).isNull();
    assertThat(problem.getDetail()).isNull();
  }

  @Test
  void statusBoundariesAreAccepted() {
    assertThat(ProblemDetail.builder().status(100).build().getStatus()).isEqualTo(100);
    assertThat(ProblemDetail.builder().status(599).build().getStatus()).isEqualTo(599);
  }

  @Test
  void statusOutsideHttpRangeIsRejected() {
    assertThatThrownBy(() -> ProblemDetail.builder().status(99).build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("99");
    assertThatThrownBy(() -> ProblemDetail.builder().status(600).build())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("600");
  }

  @Test
  void typedExtensionsRoundTrip() {
    ProblemDetailKey<Integer> balance = ProblemDetailKey.of("balance", Integer.class);
    ProblemDetailKey<String[]> accounts = ProblemDetailKey.of("accounts", String[].class);

    ProblemDetail problem =
        ProblemDetail.builder()
            .extension(balance, 30)
            .extension(accounts, new String[] {"/account/12345", "/account/67890"})
            .build();

    assertThat(problem.get(balance)).contains(30);
    assertThat(problem.get(accounts).orElseThrow())
        .containsExactly("/account/12345", "/account/67890");
    assertThat(problem.has(balance)).isTrue();
    assertThat(problem.has(ProblemDetailKey.of("missing", String.class))).isFalse();
    assertThat(problem.get(ProblemDetailKey.of("missing", String.class))).isEmpty();
  }

  @Test
  void extensionLookupRejectsNullKey() {
    ProblemDetail problem = ProblemDetail.builder().build();

    assertThatThrownBy(() -> problem.get(null)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> problem.has(null)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> ProblemDetail.builder().extension(null, "x"))
        .isInstanceOf(NullPointerException.class);
    assertThatThrownBy(
            () -> ProblemDetail.builder().extension(ProblemDetailKey.of("x", String.class), null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void sameNameKeysShareOneMemberWithLastValueWinning() {
    ProblemDetailKey<String> first = ProblemDetailKey.of("member", String.class);
    ProblemDetailKey<Integer> second = ProblemDetailKey.of("member", Integer.class);

    ProblemDetail problem =
        ProblemDetail.builder().extension(first, "text").extension(second, 42).build();

    assertThat(problem.get(second)).contains(42);
    assertThat(problem.extensionMembers()).isEqualTo(Map.of("member", 42));
  }

  @Test
  void extensionMembersViewIsUnmodifiableSnapshotInInsertionOrder() {
    ProblemDetailKey<Integer> balance = ProblemDetailKey.of("balance", Integer.class);
    ProblemDetailKey<String> note = ProblemDetailKey.of("note", String.class);

    ProblemDetail problem =
        ProblemDetail.builder().extension(balance, 30).extension(note, "low").build();

    assertThat(problem.extensionMembers())
        .containsExactly(Map.entry("balance", 30), Map.entry("note", "low"));
    assertThatThrownBy(() -> problem.extensionMembers().put("extra", 1))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void builtInstanceIsImmuneToLaterBuilderMutations() {
    ProblemDetailKey<Integer> balance = ProblemDetailKey.of("balance", Integer.class);
    ProblemDetail.Builder builder = ProblemDetail.builder().title("before").extension(balance, 1);

    ProblemDetail first = builder.build();
    builder.title("after").extension(balance, 2);
    ProblemDetail second = builder.build();

    assertThat(first.getTitle()).isEqualTo("before");
    assertThat(first.get(balance)).contains(1);
    assertThat(second.getTitle()).isEqualTo("after");
    assertThat(second.get(balance)).contains(2);
  }

  @Test
  void valueSemantics() {
    ProblemDetailKey<Integer> balance = ProblemDetailKey.of("balance", Integer.class);

    ProblemDetail first =
        ProblemDetail.builder().title("Title").status(400).extension(balance, 30).build();
    ProblemDetail second =
        ProblemDetail.builder().title("Title").status(400).extension(balance, 30).build();
    ProblemDetail different = ProblemDetail.builder().title("Other").status(400).build();

    assertThat(first)
        .isEqualTo(second)
        .hasSameHashCodeAs(second)
        .isNotEqualTo(different)
        .isNotNull();
    assertThat(second).isEqualTo(first);
  }

  @Test
  void inequalityIsDetectedPerMember() {
    ProblemDetailKey<Integer> balance = ProblemDetailKey.of("balance", Integer.class);
    ProblemDetail base =
        ProblemDetail.builder()
            .type("https://example.com/probs/a")
            .title("Title")
            .status(400)
            .detail("Detail")
            .instance("/instances/1")
            .extension(balance, 30)
            .build();

    assertThat(base)
        .isNotEqualTo(
            ProblemDetail.builder()
                .type("https://example.com/probs/b")
                .title("Title")
                .status(400)
                .detail("Detail")
                .instance("/instances/1")
                .extension(balance, 30)
                .build())
        .isNotEqualTo(
            ProblemDetail.builder()
                .type("https://example.com/probs/a")
                .title("Other")
                .status(400)
                .detail("Detail")
                .instance("/instances/1")
                .extension(balance, 30)
                .build())
        .isNotEqualTo(
            ProblemDetail.builder()
                .type("https://example.com/probs/a")
                .title("Title")
                .status(404)
                .detail("Detail")
                .instance("/instances/1")
                .extension(balance, 30)
                .build())
        .isNotEqualTo(
            ProblemDetail.builder()
                .type("https://example.com/probs/a")
                .title("Title")
                .status(400)
                .detail("Other")
                .instance("/instances/1")
                .extension(balance, 30)
                .build())
        .isNotEqualTo(
            ProblemDetail.builder()
                .type("https://example.com/probs/a")
                .title("Title")
                .status(400)
                .detail("Detail")
                .instance("/instances/2")
                .extension(balance, 30)
                .build())
        .isNotEqualTo(
            ProblemDetail.builder()
                .type("https://example.com/probs/a")
                .title("Title")
                .status(400)
                .detail("Detail")
                .instance("/instances/1")
                .extension(balance, 31)
                .build());
  }

  @Test
  void toStringRendersAllPresentMembers() {
    ProblemDetailKey<Integer> balance = ProblemDetailKey.of("balance", Integer.class);
    ProblemDetail problem =
        ProblemDetail.builder()
            .type("https://example.com/probs/out-of-credit")
            .title("Title")
            .status(403)
            .detail("Detail")
            .instance("/instances/1")
            .extension(balance, 30)
            .build();

    assertThat(problem.toString())
        .contains(
            "type=https://example.com/probs/out-of-credit",
            "title=Title",
            "status=403",
            "detail=Detail",
            "instance=/instances/1",
            "balance=30");
  }

  @Test
  void toStringOmitsAbsentMembers() {
    assertThat(ProblemDetail.builder().build()).hasToString("ProblemDetail[type=about:blank]");
  }

  @Test
  void relativeTypeReferenceIsPreserved() {
    ProblemDetail problem = ProblemDetail.builder().type("/types/out-of-credit").build();

    assertThat(problem.getType()).isEqualTo(URI.create("/types/out-of-credit"));
  }
}
