package com.githubtimemachine.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.git")
@Getter
@Setter
public class GitProperties {

    private String baseDirectory;
    private String defaultBranch;
}
