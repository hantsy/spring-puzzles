package com.example.demo

import org.springframework.context.annotation.Configuration
import org.springframework.integration.annotation.IntegrationComponentScan
import org.springframework.integration.config.EnableIntegration


@Configuration
@EnableIntegration
@IntegrationComponentScan(basePackageClasses = [UploadGateway::class])
class IntegrationConfig {
}