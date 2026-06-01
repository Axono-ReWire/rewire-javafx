package com.axono.resources;

import com.axono.ui.UITheme;
import com.axono.ui.UIConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * ResourceView displays all available learning resource
 * and modules within the application.
 *
 * Users can browse module descriptions and open corresponding notes
 * via the "open notes" button.
 



         * Constructor for ResourceView.

        public ResourceView() {
                buildUI();
        }

           
            * Builds main layout of the pa
            * Creates the scrollable layout and page structu
           
            * Applies theme to match the styling of the applicat
            * and previous pag
           

            VBox content = new V
                content.setAlignment(Pos.TOP_CENTER);
                content.setPadding(new Insets(
                        UIConstants.CONTENT_PA
                                UIConstants.PADDING_MD,
                                UIConstants.CONTENT_PAD
                                UIConstants.PADDING_MD));
                                setMaxWidth(UIConstants.C
                content.setStyle(BG_COLOR_STYLE + UITheme.BG + ";")
                

                        buildHeader(),
                                buildResources
                                

                wrapper.setAlignment(Pos.TOP_CENT
                wrapper.setStyle(BG_COLOR_STYLE + UIT
                HBox.setHgrow(content, Priority.ALWAYS);
                

                setFitToWidth(true);
                setBorder(Border.EMP
                setStyle(BG_COLOR_STYLE 
                
        

           
             *
           
             *
             * @return VBox containing the
           
          
           

                    + "-fx-font-weig
                        + "-fx-text-fill: " + UITheme.TEXT_DAR
                
                                tons = new HBox(UIConstant
                                outlineButton("Back"),

                buttons.setAlignment(Pos.CENTER);
                                
                                der = new VBox(UIConstants.P
                header.setAlignment(Pos.CENTER);

                return header;
                

                
         

           
             * - Generic description
           
             * - Button to link to the notes
             *
             * @return VBox co
             */
            private VBox buildResource
           
                  
         

                    buildResourceBox("Introduc
                                "Access structured notes and
                                        "This module intro

                                        + "from Newtonian mechani
                                                      + "properties of material
                                                        + "circuit analysis, and an introductio
                                                        + "logic.",
                                                                "Open Notes"),
                                                                ourceBox("Mathematics",
                                                                "Access structured notes and relevant content",
                                                                "Mathematics is the most important tool an engineer "
                                                        + "has
                                                      + "for fo
                                                             + "Mathematics lies at the heart o
                                                             + "offers unparalleled insights into the
                                                                             + "the natural world. This module develops
                                                                             + "and confidence in a range of mathematica
                                                                             + "necessary for the analysis, design, and explo
                                                                             + "of engineering systems.",
                                                                     "Open Notes"),
                                                                ldResourceBox("Programming and Digital Interfacing",
                                                                     "Access structured note
                                                This module wi
                                                      + "programming, and how it can be
                                                             + "help create interactive enginee
                                                             + "start from the very beginning, looking at the
                                                                             + "of programming, before moving on to look at more 
                                                                             + "advanced concepts and techniques, using a restr
                                                                             + "subset of the C++11 programming language.",
                                                                     "Open Notes"),
                                                                ldResourceBox("Digital Electronics",
                                                Access structu
                                              "The module will introduc
                                                             + "the design and implementation o
                                                             + "circuits, covering technology and design flows "
                                                                             + "targeting application-specific integrated circu
                                                                             + "(ASICs) and field-programmable gate arrays (FPG
                                                                             + "Techniques to improve performance will be considered "
                                                                             + "at different levels. Technology scaling, performance "
                                                                             + "(timing)/power/area (PPA), standard cell librar
                                                                             + "full-/semi-custom ASIC design. Timing, pipeli
                                                                             + "clock domain crossing, place and route on FPGA. "
                                                                             + "Approaches for designing testable circuits will be
                                                                             + "developed, including verification, fault models, "
                                                       + "desi
                                              "Open Notes"),
                                                ldResourceBox("Controls, Sensors and Instrument

                                                     "This module provides an introduction to feedback "
                                                                             + "control of linear systems, and how it can be used "
                                                                             + "to provide stability or to obtain a particular "
                                                                             + "response characteristic from a system. The techniques "
                                                                             + "covered have a wide range of applic
                                                                             + "to mechanical systems such as robots, and to "
                                                                             + "electronic systems such as audio amplifiers. Feedback "
                                                                             + "control requires that the system under control be "
                                                                             + "instrumented so that the controller knows what it is "
                                                       + "doin
                                                      + "types in this module, inclu
                                                             + "micromechanical, and their rela
                                                             + "and performance.",
                                                                     "Open Notes"),
                                                                ldResourceBox("Ma
                                                Access structu
                                              "This module introdu
                                                             + "tools that are useful for model
                                                             + "engineering systems and for the analysis and "
                                                                             + "processing of signals.",
                                                                     "Open Notes"),
                                                                ldResourceBox("Circuit Design",
                                                                     "Access structured notes and relevant content",
                                                                     "This module introduces students to analogue and digital "
                                                                             + "design concepts, along with the appropriate role of "
                                                                             + "Hardware Description Languages (HDLs) and simulation "
                                                                             + "in the mo
                                                            + 

                                                      + "appropriate testing through the use of HDL 
                                                             + "benches and simulation. The pro
                                                             + "of semiconductor devices, small signal models and "
                                                                             + "device design will be covered.",
                                                                     "Open Notes"),
                                                                ldResourceBox("Communication systems and Digital "
                                                                     + "Communications",
                                                                     "Access structured notes and relevant 
                                                The Communicat
                                                      + "detailed understandi
                                                             + "communication systems work, fro
                                                             + "through to the design of practical radio systems and "
                                                                             + "networks. Topics include: information theory; wireless "
                                                                             + "link design; signals, baseband and passband radio "
                                                                             + "modulation and demodulation; transmitter and receiver "
                                                                             + "architectures, networks and protocols.",
                                                                     "Open Notes"),
                                                                ldResourceBox("Digital Signal Processing",
                                                                     "Access structured notes and relevant content",
                                                                     "We will introduce discrete time techniques routinely "
                                                                             + "used in Digital Signal Processing (DSP) systems, "
                                                                             + "including the discrete time Fourier transform (DTFT), "
                                                                             + "discrete Fourier transform (DFT) and discrete time "
                                                                             + "convolution and correlation. The importance of data "
                                                                             + "windows in DSP will be highlighted and a range of "
                                                                             + "data windows will be introduced, includ
                                                       + "cosi
                                                      + "orthogonal mul
                                                             + "analysis of signals will be des
                                                             + "practical aspects of spectral leakage, analysis of "
                                                                             + "stochastic signals and time-frequency analysis using "
                                                                             + "spectrograms. Practical applications of these "
                                                                             + "techniques will be considered using a range of "
                                                                             + "different data modalities including biomedical, "
                                                                             + "environmental and speech data. The difference equation "
                                                                             + "as a key design tool in DSP will be int
                                                       + "its

                  

                                        + "Machine learning in 
                                             
                                                + "Ne
                                        + "image recog
                                        + "applications (MNIST, Ima

                        buildRe
         

           
                                    + "circuits, covering techn
                                    + "targeting application-sp
           
         *             
                            
                             
                            
                  
           

                            "Open Notes")
                        
                        
                        Constants.SPACING_XL

                        cardWrap(resources));
                section.setAlignment(Pos.CENTER_LEFT);
                                setMaxWidth(UIConstants.SE
                                

                
                
                                

                
                akes the design and formatting to match a
                                
                param title the title of the 
                param subheading the subhea
                param description the full description of
                                ttonText the text displayed on the button
                                Box containing the formatted resource box

                ate VBox buildResourceBox(fi nal S tr

                   
                                                final String b
                 
                     Label titleLabel = new Label(title);
                   

                        + "-fx-text-fill: " + UITh
                        

                        abel.setStyle("-fx
                                + "-fx-text-fill: " + UIThem SE CONDAR
                        
                                scLabel = new Label(descripti ); 
                        L
                   

                descLabel.setMaxWidth(descLabelWidth);
                                l.setStyle(
                                + "-fx-te
                                + "-fx-cu

                
                descLabel.setOnMouseCl
         

           
                       descLabel.setWrapText(true);
           
                       descLabel.setWrapText(false);
                   }
           
         
               VBo
           
                    subLabel,
                        descLabel
                );
                
                                FillWidth(true);
                                ardWrap(card);
                                
                
        /

           
            *
           
         *  * Takes th
            * Adds pad
            * to m
           

         * @return VBox with card styling applied
                
                ate VBox cardWrap(final V
                                setPadding(new Insets(
                                                dth(UIConstants.CONTENT_MA
                                                (BG_COLOR_STYLE + UIThe
                                + "-fx-border-color: " + U
                        +
         

           
         
           
            * Creates a styled section heading label.
            *
           
            * @param s
            * @ret
           

            Label l = new Label(text);
                l.setStyle(String.format(
                                "-fx-font-size: %dpx;"
                                        + "-fx-font-weight
                                        + "-fx-text-fill: %
                                size, UITheme.TEXT_DARK));
                                ;
                                
                                

                reates a themed outline button.
                                

                nteraction and feedback.
                
                param text the text displayed on the button
                return Button with outline styling applied
                
        private Button outlineButton(final String text) {
        String base = "-fx-background-color: transparent;"
                + "-fx-border-color: " + UITheme.PRIMARY + ";"
                + "-fx-border-width: 2px;"
                + "-fx-border-radius: 4px;"
                + "-fx-background-radius: 4px;"
                + "-fx-text-fill: " + UITheme.PRIMARY + ";"
                + "-fx-font-weight: bold;"
                + "-fx-font-size: 14px;";

        String hover = BG_COLOR_STYLE + UITheme.PRIMARY + ";"
                + "-fx-text-fill: white;";

        Button b = new Button(text);
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(hover));
        b.setOnMouseExited(e -> b.setStyle(base));
        return b;
    }
}