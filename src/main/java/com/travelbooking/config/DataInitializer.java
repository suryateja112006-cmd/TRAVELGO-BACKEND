package com.travelbooking.config;

import com.travelbooking.entity.TravelPackage;
import com.travelbooking.entity.User;
import com.travelbooking.repository.TravelPackageRepository;
import com.travelbooking.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

/**
 * DataInitializer runs on application startup to seed default travel packages
 * and demo user/admin accounts if the database is initially empty.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TravelPackageRepository packageRepository;

    public DataInitializer(UserRepository userRepository, TravelPackageRepository packageRepository) {
        this.userRepository = userRepository;
        this.packageRepository = packageRepository;
    }

    @Override
    public void run(String... args) {
        // Seed default users if table is empty
        if (userRepository.count() == 0) {
            User admin = new User(
                    null,
                    "System Administrator",
                    "admin@travelgo.com",
                    "9999999999",
                    "admin123",
                    "ADMIN"
            );

            User demoUser = new User(
                    null,
                    "John Doe",
                    "john@example.com",
                    "9876543210",
                    "user123",
                    "USER"
            );

            userRepository.saveAll(Arrays.asList(admin, demoUser));
            System.out.println(">>> Seeded default Admin and User accounts into MySQL.");
        }

        // Seed travel packages if table is empty
        if (packageRepository.count() == 0) {
            TravelPackage pkg1 = new TravelPackage(
                    null,
                    "Goa Beach Escape",
                    "Goa",
                    "Enjoy the golden sands, vibrant beach shacks, water sports, and Portuguese architecture in North and South Goa.",
                    "3 Days",
                    8999.00
            );

            TravelPackage pkg2 = new TravelPackage(
                    null,
                    "Manali Adventure",
                    "Manali",
                    "Experience snow valleys, Solang valley sports, Rohtang Pass, and scenic pine-covered Himalayan trails.",
                    "5 Days",
                    14999.00
            );

            TravelPackage pkg3 = new TravelPackage(
                    null,
                    "Kerala Backwaters",
                    "Kerala",
                    "Relax on a tranquil houseboat cruise across Alleppey backwaters, tea plantations of Munnar, and spice gardens.",
                    "4 Days",
                    11999.00
            );

            TravelPackage pkg4 = new TravelPackage(
                    null,
                    "Hyderabad Heritage Tour",
                    "Hyderabad",
                    "Explore the historic Charminar, Golconda Fort, Salar Jung Museum, and savor authentic Hyderabadi Biryani.",
                    "2 Days",
                    5999.00
            );

            TravelPackage pkg5 = new TravelPackage(
                    null,
                    "Jaipur Royal Trip",
                    "Jaipur",
                    "Witness the regal magnificence of Amber Fort, Hawa Mahal, City Palace, and vibrant Rajasthani cultural bazaars.",
                    "4 Days",
                    10999.00
            );

            packageRepository.saveAll(Arrays.asList(pkg1, pkg2, pkg3, pkg4, pkg5));
            System.out.println(">>> Seeded 5 sample travel packages into MySQL.");
        }

        Map<String, String> samplePlaces = Map.of(
                "Goa Beach Escape", "Baga Beach\nFort Aguada\nBasilica of Bom Jesus\nAnjuna Flea Market",
                "Manali Adventure", "Hadimba Devi Temple\nSolang Valley\nAtal Tunnel\nOld Manali",
                "Kerala Backwaters", "Alleppey Backwaters\nMunnar Tea Gardens\nMattupetty Dam\nFort Kochi",
                "Hyderabad Heritage Tour", "Charminar\nGolconda Fort\nSalar Jung Museum\nChowmahalla Palace",
                "Jaipur Royal Trip", "Amber Fort\nHawa Mahal\nCity Palace\nJantar Mantar"
        );
        Map<String, String> samplePlaceImages = Map.of(
                "Goa Beach Escape", String.join("\n",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/8/80/Baga_Beach_25012016.jpg/960px-Baga_Beach_25012016.jpg",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/9/95/Fort_Aguada_Remote_view_25012016.jpg/960px-Fort_Aguada_Remote_view_25012016.jpg",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/d/de/Side_Elevation_of_Basilica_of_Bom_Jesus.jpg/960px-Side_Elevation_of_Basilica_of_Bom_Jesus.jpg",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1b/Anjuna%2C_Goa%2C_India%2C_Anjuna_Flea_Market_2.jpg/960px-Anjuna%2C_Goa%2C_India%2C_Anjuna_Flea_Market_2.jpg"
                ),
                "Manali Adventure", String.join("\n",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/2/28/Hadimba_Devi_Temple_Manali.jpg/960px-Hadimba_Devi_Temple_Manali.jpg",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b5/Solang_Valley%2C_Manali.jpg/960px-Solang_Valley%2C_Manali.jpg",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4a/PM_Narendra_Modi_stands_at_the_entrance_to_the_Atal_Tunnel_in_Rohtang.jpg/960px-PM_Narendra_Modi_stands_at_the_entrance_to_the_Atal_Tunnel_in_Rohtang.jpg",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1a/Old_Manali%2C_Manali%2C_India_%28Unsplash_MSK-2kuYJDE%29.jpg/960px-Old_Manali%2C_Manali%2C_India_%28Unsplash_MSK-2kuYJDE%29.jpg"
                ),
                "Kerala Backwaters", String.join("\n",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/d/d5/Alleppey_backwaters_view_%2C_Kerala_%2C_India_15.jpg/960px-Alleppey_backwaters_view_%2C_Kerala_%2C_India_15.jpg",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/3/38/Munnar_tea_gardens.jpg/960px-Munnar_tea_gardens.jpg",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/a/af/Mattupetty_Lake_View.jpg/960px-Mattupetty_Lake_View.jpg",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e2/Chinese_Fishing_Nets_-_2.jpg/960px-Chinese_Fishing_Nets_-_2.jpg"
                ),
                "Hyderabad Heritage Tour", String.join("\n",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/7/71/Charminar_Hyderabad_1.jpg/960px-Charminar_Hyderabad_1.jpg",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/1/12/Golconda_Fort%2C_Hyderabad_%28Oct_2025%29_-_3.jpg/960px-Golconda_Fort%2C_Hyderabad_%28Oct_2025%29_-_3.jpg",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c1/Salar_Jung_Museum%2C_the_building_and_complex_in_Hyderabad_21.jpg/960px-Salar_Jung_Museum%2C_the_building_and_complex_in_Hyderabad_21.jpg",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/7/78/Chowmahalla_Palace_gate_tower_01.jpg/960px-Chowmahalla_Palace_gate_tower_01.jpg"
                ),
                "Jaipur Royal Trip", String.join("\n",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f7/Jaipur_03-2016_02_Amber_Fort.jpg/960px-Jaipur_03-2016_02_Amber_Fort.jpg",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/4/41/East_facade_Hawa_Mahal_Jaipur_from_ground_level_%28July_2022%29_-_img_01.jpg/960px-East_facade_Hawa_Mahal_Jaipur_from_ground_level_%28July_2022%29_-_img_01.jpg",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/4/40/Jaipur_03-2016_20_City_Palace_complex.jpg/960px-Jaipur_03-2016_20_City_Palace_complex.jpg",
                        "https://thumb.wikimedia.org/wikipedia/commons/thumb/8/80/Doors_of_Jantar_Mantar.jpg/960px-Doors_of_Jantar_Mantar.jpg"
                )
        );
        Map<String, String> samplePlaceImageSources = Map.of(
                "Goa Beach Escape", String.join("\n",
                        "https://commons.wikimedia.org/wiki/File:Baga_Beach_25012016.jpg",
                        "https://commons.wikimedia.org/wiki/File:Fort_Aguada_Remote_view_25012016.jpg",
                        "https://commons.wikimedia.org/wiki/File:Side_Elevation_of_Basilica_of_Bom_Jesus.jpg",
                        "https://commons.wikimedia.org/wiki/File:Anjuna,_Goa,_India,_Anjuna_Flea_Market_2.jpg"
                ),
                "Manali Adventure", String.join("\n",
                        "https://commons.wikimedia.org/wiki/File:Hadimba_Devi_Temple_Manali.jpg",
                        "https://commons.wikimedia.org/wiki/File:Solang_Valley,_Manali.jpg",
                        "https://commons.wikimedia.org/wiki/File:PM_Narendra_Modi_stands_at_the_entrance_to_the_Atal_Tunnel_in_Rohtang.jpg",
                        "https://commons.wikimedia.org/wiki/File:Old_Manali,_Manali,_India_(Unsplash_MSK-2kuYJDE).jpg"
                ),
                "Kerala Backwaters", String.join("\n",
                        "https://commons.wikimedia.org/wiki/File:Alleppey_backwaters_view_,_Kerala_,_India_15.jpg",
                        "https://commons.wikimedia.org/wiki/File:Munnar_tea_gardens.jpg",
                        "https://commons.wikimedia.org/wiki/File:Mattupetty_Lake_View.jpg",
                        "https://commons.wikimedia.org/wiki/File:Chinese_Fishing_Nets_-_2.jpg"
                ),
                "Hyderabad Heritage Tour", String.join("\n",
                        "https://commons.wikimedia.org/wiki/File:Charminar_Hyderabad_1.jpg",
                        "https://commons.wikimedia.org/wiki/File:Golconda_Fort,_Hyderabad_(Oct_2025)_-_3.jpg",
                        "https://commons.wikimedia.org/wiki/File:Salar_Jung_Museum,_the_building_and_complex_in_Hyderabad_21.jpg",
                        "https://commons.wikimedia.org/wiki/File:Chowmahalla_Palace_gate_tower_01.jpg"
                ),
                "Jaipur Royal Trip", String.join("\n",
                        "https://commons.wikimedia.org/wiki/File:Jaipur_03-2016_02_Amber_Fort.jpg",
                        "https://commons.wikimedia.org/wiki/File:East_facade_Hawa_Mahal_Jaipur_from_ground_level_(July_2022)_-_img_01.jpg",
                        "https://commons.wikimedia.org/wiki/File:Jaipur_03-2016_20_City_Palace_complex.jpg",
                        "https://commons.wikimedia.org/wiki/File:Doors_of_Jantar_Mantar.jpg"
                )
        );
        for (TravelPackage travelPackage : packageRepository.findAll()) {
            boolean changed = false;
            String places = samplePlaces.get(travelPackage.getName());
            if ((travelPackage.getPlaces() == null || travelPackage.getPlaces().isBlank()) && places != null) {
                travelPackage.setPlaces(places);
                changed = true;
            }
            String images = samplePlaceImages.get(travelPackage.getName());
            if ((travelPackage.getPlaceImages() == null || travelPackage.getPlaceImages().isBlank()) && images != null) {
                travelPackage.setPlaceImages(images);
                changed = true;
            }
            String imageSources = samplePlaceImageSources.get(travelPackage.getName());
            if ((travelPackage.getPlaceImageSources() == null || travelPackage.getPlaceImageSources().isBlank())
                    && imageSources != null) {
                travelPackage.setPlaceImageSources(imageSources);
                changed = true;
            }
            if (changed) {
                packageRepository.save(travelPackage);
            }
        }
    }
}
