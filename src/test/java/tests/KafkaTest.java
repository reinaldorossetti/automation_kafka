package tests;

import kafka.ConsumerUser;
import kafka.ProducerUser;
import kafkaAvro.ConsumerAvroUser;
import kafkaAvro.ProducerAvroUser;
import modelAvro.user.UserAvro;
import org.apache.kafka.common.errors.SerializationException;
import org.junit.Test;
import user.User;
import utils.ReadYml;
import com.github.javafaker.Faker;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public class KafkaTest {
    User user;
    Faker faker = new Faker();

    @Test
    public void enviandoMensagemParaOTopicoComSucesso(){
        // Lendo arquivo yml
        Properties dados = ReadYml.lerArquivoYml("dados");

        // Adicionando valores ao objeto users
        User users = user.builder().nome(dados.getProperty("nome")).email(dados.getProperty("email")).age(1.0F).build();

        // Enviando uma mensagem pro tópico "topic_user" com o valor "{"nome":bruno,"email":bruno@gmail.com,"age":1}"
        ProducerUser.sendMessageTopic("topic_user", users);

        // Consumindo uma mensagem do tópico e validando se esta correto
        assertThat(ConsumerUser.getMessageTopic("topic_user"), is("{\"nome\":bruno,\"email\":bruno@gmail.com,\"age\":1.0}"));
    }

    @Test
    public void enviandoMensagemParaOTopicoComSucessoComAvro(){
        // setando valores pro objeto UserAvro
        UserAvro avroMessage = UserAvro.newBuilder()
                .setNome(faker.name().fullName())
                .setEmail(faker.internet().emailAddress())
                .setAge(String.valueOf(faker.number().numberBetween(18, 80)))
                .build();

        // Enviando uma mensagem pro tópico "topic_user" com o valor do faker
        ProducerAvroUser.sendMessageTopic("topic_user", avroMessage);

        // Consumindo uma mensagem do tópico e validando se esta correto
        assertThat(ConsumerAvroUser.getMessageTopic("topic_user"), is(avroMessage.toString()));
    }

    @Test
    public void enviandoMensagemParaOTopicoComFalhaComAvro(){

        // setando valores pro objeto UserAvro
        UserAvro avroMessage = UserAvro.newBuilder()
                .setNome(faker.name().fullName())
                .setEmail(faker.internet().emailAddress())
                .setAge(faker.number().numberBetween(18, 80))
                .build();

        try {
            // Deve lançar SerializationException
            ProducerAvroUser.sendMessageTopic("topic_user", avroMessage);
        } catch (SerializationException e) {
            // Validando a mensagem de erro retornada pelo producer
            assertThat(e.getMessage(), containsString("Error serializing Avro message"));
        }
    }
}
