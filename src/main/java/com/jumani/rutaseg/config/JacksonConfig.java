package com.jumani.rutaseg.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.jumani.rutaseg.util.DateUtil;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.ZonedDateTime;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .serializers(this.dateSerializer())
                .deserializers(this.dateDeserializer())
                .featuresToEnable(
                        DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL,
                        MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS
                );
    }

    private StdDeserializer<ZonedDateTime> dateDeserializer() {
        return new StdDeserializer<>(ZonedDateTime.class) {
            @Override
            public ZonedDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return DateUtil.stringToZonedDateTimeUTC(p.getText());
            }
        };
    }

    private StdSerializer<ZonedDateTime> dateSerializer() {
        return new StdSerializer<>(ZonedDateTime.class) {
            @Override
            public void serialize(ZonedDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                gen.writeString(DateUtil.zonedDateTimeToStringUTC(value));
            }
        };
    }
}
