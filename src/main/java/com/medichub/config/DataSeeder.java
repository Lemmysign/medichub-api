package com.medichub.config;

import com.medichub.config.SubscriptionPlanProperties;
import com.medichub.model.PlatformSettings;
import com.medichub.model.SubscriptionPlan;
import com.medichub.repository.PlatformSettingsRepository;
import com.medichub.repository.SubscriptionPlanRepository;
import com.medichub.repository.UserRepository;
import com.medichub.security.DisabledUserRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Startup seeding: the singleton {@link PlatformSettings} row and the single
 * {@link SubscriptionPlan} (CLAUDE.md §4, §6). The plan price is read from config —
 * never hardcoded — so seeding is skipped until a price is supplied.
 */
@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    public ApplicationRunner seedPlatformSettings(PlatformSettingsRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                PlatformSettings settings = new PlatformSettings();
                settings.setVideoDownloadEnabled(false);
                repository.save(settings);
                log.info("Seeded PlatformSettings singleton (videoDownloadEnabled=false)");
            }
        };
    }

    @Bean
    public ApplicationRunner seedSubscriptionPlan(SubscriptionPlanRepository planRepository,
                                                  SubscriptionPlanProperties planProps) {
        return args -> {
            if (planRepository.count() > 0) {
                return;
            }
            if (planProps.priceKobo() == null || planProps.priceKobo() <= 0) {
                log.warn("No subscription plan seeded — set SUBSCRIPTION_PLAN_PRICE_KOBO to configure the price.");
                return;
            }
            SubscriptionPlan plan = new SubscriptionPlan();
            plan.setName(planProps.name());
            plan.setPriceKobo(planProps.priceKobo());
            plan.setCurrency(planProps.currency() == null ? "NGN" : planProps.currency());
            plan.setIntervalDays(planProps.intervalDays() == null ? 30 : planProps.intervalDays());
            plan.setActive(true);
            planRepository.save(plan);
            log.info("Seeded SubscriptionPlan '{}' ({} kobo / {} days)",
                    plan.getName(), plan.getPriceKobo(), plan.getIntervalDays());
        };
    }

    /** Load already-disabled accounts into the in-memory registry so restarts keep blocking them. */
    @Bean
    public ApplicationRunner seedDisabledUserRegistry(UserRepository userRepository,
                                                      DisabledUserRegistry disabledUserRegistry) {
        return args -> {
            List<Long> disabledIds = userRepository.findDisabledUserIds();
            disabledIds.forEach(disabledUserRegistry::markDisabled);
            if (!disabledIds.isEmpty()) {
                log.info("Loaded {} disabled account(s) into the access registry", disabledIds.size());
            }
        };
    }
}
