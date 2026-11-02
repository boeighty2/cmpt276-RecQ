package com.cmpt276g12.RecQ.admin;

import com.cmpt276g12.RecQ.facility.Court;
import com.cmpt276g12.RecQ.facility.CourtService;
import com.cmpt276g12.RecQ.facility.CourtStatus;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Staff-facing venue, sport, and court management with manual opening/closing. */
@Controller
public class AdminCourtController {
    private final CourtService service;
    private final StaffRoleGuard staffGuard;

    public AdminCourtController(CourtService service, StaffRoleGuard staffGuard) {
        this.service = service;
        this.staffGuard = staffGuard;
    }

    @GetMapping("/admin/courts")
    public String dashboard(HttpServletRequest request, Model model) {
        staffGuard.requireStaff(request);
        List<Court> courts = service.listCourts();
        Map<String, List<Court>> venues = courts.stream().collect(Collectors.groupingBy(
            Court::getGymName, LinkedHashMap::new, Collectors.toList()));
        List<VenueSummary> cards = new ArrayList<>();
        // The home page needs each sport's status, not just its name.
        // Keep this server-side so the colored pills reflect persisted data.
        Map<String, Map<String, Boolean>> venueSportStatuses = new LinkedHashMap<>();
        venues.forEach((name, venueCourts) -> {
            Map<String, Boolean> sportStatuses = new LinkedHashMap<>();
            for (Court court : venueCourts) {
                sportStatuses.putIfAbsent(court.getSportName(), court.isSportOpen());
            }
            venueSportStatuses.put(name, sportStatuses);
            List<String> sportNames = venueCourts.stream().map(Court::getSportName)
                .distinct().sorted(String.CASE_INSENSITIVE_ORDER).toList();
            cards.add(new VenueSummary(name, venueCourts.size(),
                count(venueCourts, CourtStatus.AVAILABLE),
                count(venueCourts, CourtStatus.OCCUPIED),
                count(venueCourts, CourtStatus.CLOSED), sportNames.size(), sportNames));
        });
        model.addAttribute("venues", cards);
        model.addAttribute("venueSportStatuses", venueSportStatuses);
        return "admin/venues";
    }

    @GetMapping("/admin/courts/venue")
    public String venue(HttpServletRequest request, @RequestParam String gym, Model model) {
        staffGuard.requireStaff(request);
        List<Court> courts = service.listCourts().stream()
            .filter(c -> c.getGymName().equals(gym)).toList();
        if (courts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found");
        }
        model.addAttribute("gym", gym);
        // One row per sport; legacy dated court records remain grouped under their sport.
        Map<String, List<Court>> sports = courts.stream().collect(Collectors.groupingBy(
            Court::getSportName, LinkedHashMap::new, Collectors.toList()));
        List<SportSummary> sportItems = new ArrayList<>();
        sports.forEach((name, grouped) -> sportItems.add(new SportSummary(name, grouped.size(),
            count(grouped, CourtStatus.AVAILABLE), count(grouped, CourtStatus.OCCUPIED),
            count(grouped, CourtStatus.CLOSED), grouped.get(0).isSportOpen())));
        model.addAttribute("sportItems", sportItems);
        return "admin/venue";
    }

    @PostMapping("/admin/courts/venue/rename")
    public String renameVenue(HttpServletRequest request,
                              @RequestParam String oldName,
                              @RequestParam String newName,
                              RedirectAttributes messages) {
        staffGuard.requireStaff(request);
        try {
            service.renameVenue(oldName, newName);
            messages.addFlashAttribute("success", oldName + " renamed to " + newName.trim() + ".");
        } catch (IllegalArgumentException | NoSuchElementException ex) {
            messages.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/courts";
    }

    @PostMapping("/admin/courts/venue/delete")
    public String deleteVenue(HttpServletRequest request,
                              @RequestParam String venueName,
                              RedirectAttributes messages) {
        staffGuard.requireStaff(request);
        try {
            int deletedCourts = service.deleteVenue(venueName);
            messages.addFlashAttribute("success", venueName + " deleted ("
                + deletedCourts + " courts removed).");
        } catch (IllegalArgumentException | NoSuchElementException ex) {
            messages.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/courts";
    }

    @PostMapping("/admin/courts/sport/rename")
    public String renameSport(HttpServletRequest request,
                              @RequestParam String gym,
                              @RequestParam String sport,
                              @RequestParam String newName,
                              RedirectAttributes messages) {
        staffGuard.requireStaff(request);
        try {
            service.renameSport(gym, sport, newName);
            messages.addFlashAttribute("success", gym + " — " + sport
                + " renamed to " + newName.trim() + ".");
        } catch (IllegalArgumentException | NoSuchElementException ex) {
            messages.addFlashAttribute("error", ex.getMessage());
        }
        return redirectToVenue(messages, gym);
    }

    @PostMapping("/admin/courts/sport/state")
    public String changeSportState(HttpServletRequest request,
                                   @RequestParam String gym,
                                   @RequestParam String sport,
                                   @RequestParam boolean open,
                                   RedirectAttributes messages) {
        staffGuard.requireStaff(request);
        try {
            service.setSportOpen(gym, sport, open);
            messages.addFlashAttribute("success", gym + " — " + sport
                + (open ? " opened." : " closed."));
            return redirectToVenue(messages, gym);
        } catch (IllegalArgumentException | NoSuchElementException ex) {
            messages.addFlashAttribute("error", ex.getMessage());
            return redirectToVenue(messages, gym);
        }
    }

    @PostMapping("/admin/courts/sport/delete")
    public String deleteSport(HttpServletRequest request,
                              @RequestParam String gym,
                              @RequestParam String sport,
                              RedirectAttributes messages) {
        staffGuard.requireStaff(request);
        try {
            service.deleteSport(gym, sport);
            messages.addFlashAttribute("success", sport
                + " and all its courts deleted from " + gym + ".");
            // The final deleted sport also removes the venue card. Avoid a 404 on redirect.
            if (service.listCourts().stream().noneMatch(c -> c.getGymName().equalsIgnoreCase(gym))) {
                return "redirect:/admin/courts";
            }
        } catch (IllegalArgumentException | NoSuchElementException ex) {
            messages.addFlashAttribute("error", ex.getMessage());
        }
        return redirectToVenue(messages, gym);
    }

    private static String redirectToVenue(RedirectAttributes messages, String gym) {
        messages.addAttribute("gym", gym);
        return "redirect:/admin/courts/venue";
    }

    /** Show courts for a venue and sport, including any legacy schedule records. */
    @GetMapping("/admin/courts/sport")
    public String sportCourts(HttpServletRequest request,
                              @RequestParam String gym, @RequestParam String sport, Model model) {
        staffGuard.requireStaff(request);
        List<Court> courts = service.listCourts().stream()
            .filter(c -> c.getGymName().equalsIgnoreCase(gym)
                && c.getSportName().equalsIgnoreCase(sport)).toList();
        if (courts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sport not found at venue");
        }
        model.addAttribute("gym", gym);
        model.addAttribute("sport", sport);
        model.addAttribute("firstCourtId", courts.get(0).getId());
        model.addAttribute("sportIsOpen", courts.get(0).isSportOpen());
        model.addAttribute("activeRunId", service.activeRunId(gym, sport));
        model.addAttribute("courts", courts);
        model.addAttribute("availableCount", count(courts, CourtStatus.AVAILABLE));
        model.addAttribute("occupiedCount", count(courts, CourtStatus.OCCUPIED));
        model.addAttribute("closedCount", count(courts, CourtStatus.CLOSED));
        return "admin/sport-courts";
    }

    /** Backward-compatible redirect for links created by the earlier scheduled-session UI. */
    @GetMapping("/admin/courts/session")
    public String session(HttpServletRequest request,
                          @RequestParam String gym,
                          @RequestParam String sport,
                          @RequestParam String session,
                          Model model) {
        staffGuard.requireStaff(request);
        List<Court> courts = service.listCourts().stream()
            .filter(c -> c.getGymName().equals(gym)
                && c.getSportName().equals(sport)
                && c.getSessionName().equals(session)).toList();
        if (courts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sports activity not found");
        }
        // Old dated URLs remain valid, but now redirect to one manual sport view.
        return "redirect:/admin/courts/sport?gym="
            + URLEncoder.encode(gym, StandardCharsets.UTF_8)
            + "&sport=" + URLEncoder.encode(sport, StandardCharsets.UTF_8);
    }

    @GetMapping("/admin/courts/new")
    public String newSession(HttpServletRequest request,
                             @RequestParam(required = false) String gym, Model model) {
        staffGuard.requireStaff(request);
        NewSessionForm form = new NewSessionForm();
        if (gym != null) {
            form.setGym(gym);
        }
        prepareNewSessionPage(model, form);
        return "admin/new-session";
    }

    @PostMapping("/admin/courts/new")
    public String createSession(HttpServletRequest request,
                                @ModelAttribute("form") NewSessionForm form,
                                Model model, RedirectAttributes messages) {
        staffGuard.requireStaff(request);
        try {
            int courtCount = Integer.parseInt(form.getCourtCount() == null
                ? "" : form.getCourtCount().trim());
            List<Court> created = service.createSport(form.getGym(), form.getSport(), courtCount);
            Court first = created.get(0);
            messages.addFlashAttribute("success", created.size()
                + " courts created. The sport starts CLOSED until staff opens it.");
            return redirectToSport(messages, first);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex instanceof NumberFormatException
                ? "Enter a whole number of courts (1–30)." : ex.getMessage());
            prepareNewSessionPage(model, form);
            return "admin/new-session";
        }
    }

    private void prepareNewSessionPage(Model model, NewSessionForm form) {
        List<Court> courts = service.listCourts();
        model.addAttribute("form", form);
        model.addAttribute("existingVenues", courts.stream().map(Court::getGymName)
            .distinct().sorted().toList());
        model.addAttribute("existingSports", courts.stream().map(Court::getSportName)
            .distinct().sorted().toList());
    }

    @PostMapping("/admin/courts/sport/court/{id}/status")
    public String sportCourtStatus(HttpServletRequest request, @PathVariable long id,
                                   @RequestParam CourtStatus status, RedirectAttributes messages) {
        staffGuard.requireStaff(request);
        try {
            Court court = service.updateStatus(id, status);
            messages.addFlashAttribute("success", court.getCourtName() + " set to " + status + ".");
            return redirectToSport(messages, court);
        } catch (NoSuchElementException | IllegalArgumentException ex) {
            return redirectErrorToVenues(messages, ex);
        }
    }

    @PostMapping("/admin/courts/sport/court/{id}/add")
    public String sportAddCourt(HttpServletRequest request, @PathVariable long id,
                                RedirectAttributes messages) {
        staffGuard.requireStaff(request);
        try {
            Court created = service.addSportCourt(id);
            messages.addFlashAttribute("success", created.getCourtName() + " added.");
            return redirectToSport(messages, created);
        } catch (NoSuchElementException | IllegalArgumentException ex) {
            return redirectErrorToVenues(messages, ex);
        }
    }

    @PostMapping("/admin/courts/sport/court/{id}/delete")
    public String sportDeleteCourt(HttpServletRequest request, @PathVariable long id,
                                   RedirectAttributes messages) {
        staffGuard.requireStaff(request);
        try {
            Court removed = service.removeSportCourt(id);
            messages.addFlashAttribute("success", removed.getCourtName() + " deleted.");
            return redirectToSport(messages, removed);
        } catch (NoSuchElementException | IllegalArgumentException ex) {
            return redirectErrorToVenues(messages, ex);
        }
    }

    private static String redirectToSport(RedirectAttributes messages, Court court) {
        messages.addAttribute("gym", court.getGymName());
        messages.addAttribute("sport", court.getSportName());
        return "redirect:/admin/courts/sport";
    }

    private static String redirectErrorToVenues(RedirectAttributes messages, Exception ex) {
        messages.addFlashAttribute("error", ex.getMessage());
        return "redirect:/admin/courts";
    }

    private static long count(List<Court> courts, CourtStatus status) {
        return courts.stream().filter(c -> c.getStatus() == status).count();
    }

    public record SportSummary(String name, int total, long available, long occupied,
                               long closed, boolean open) {}
    public record VenueSummary(String name, int total, long available, long occupied, long closed,
                               int sportCount, List<String> sports) {}
}
