package dev.revere.commission.discord.event;

import dev.revere.commission.entities.Commission;
import dev.revere.commission.repository.CommissionRepository;
import dev.revere.commission.repository.DepartmentRepository;
import dev.revere.commission.repository.FreelancerRepository;
import dev.revere.commission.services.CommissionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Revere Development
 * @project Commission-Discord-Bot-SpringBoot
 * @date 8/27/2023
 */
@Slf4j
@Service
@AllArgsConstructor
public class ReadyEvent extends ListenerAdapter {
    private final CommissionRepository m_commissionRepository;
    private final FreelancerRepository freelancerRepository;
    private final DepartmentRepository m_departmentRepository;

    private List<String> activities;
    public static int currentStatusIndex = 0;

    @Override
    public void onReady(final net.dv8tion.jda.api.events.session.ReadyEvent event) {
        log.info("Successfully logged into " + event.getJDA().getSelfUser().getName());
        initializeActivities();
        startCyclingStatuses(event.getJDA());
    }

    private void initializeActivities() {
        activities.clear();
        activities.add("over " + freelancerRepository.count() + " freelancers");
        activities.add("over " + m_commissionRepository.count() + " commissions");
        activities.add("over " + m_departmentRepository.count() + " departments");
        activities.add("over " + m_commissionRepository
                .findAll()
                .stream()
                .filter(commission -> commission.getState() == Commission.State.COMPLETED)
                .count() + " completed commissions");
    }

    private void startCyclingStatuses(final JDA jda) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            String status = activities.get(currentStatusIndex);
            jda.getPresence().setActivity(Activity.watching(status));
            currentStatusIndex = (currentStatusIndex + 1) % activities.size();
        }, 0, 30, TimeUnit.SECONDS);
    }
}

